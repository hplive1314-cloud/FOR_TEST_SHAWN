package com.qicheng.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkspaceIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;

    private String login() throws Exception {
        var response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", "admin", "password", "Admin@123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("user.username").value("admin"))
                .andReturn().getResponse();
        return mapper.readTree(response.getContentAsString()).get("token").asText();
    }

    @Test
    void rejectsUnauthenticatedAndInvalidTokens() throws Exception {
        mvc.perform(get("/api/contents")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("status").value("UP"));
    }

    @Test
    void rejectsWrongCredentialsAndInvalidInput() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("code").value("BAD_CREDENTIALS"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"missing\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("code").value("BAD_CREDENTIALS"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("username", "admin", "password", "中".repeat(40)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatesAndRevokesTokenOnLogout() throws Exception {
        String token = login();
        assertThat(token).hasSize(43);
        assertThat(jdbc.queryForObject("SELECT password_hash FROM app_users WHERE username = 'admin'", String.class))
                .startsWith("$2").isNotEqualTo("Admin@123456");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM auth_tokens WHERE token_hash = ?", Long.class, token)).isZero();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("username").value("admin"))
                .andExpect(jsonPath("passwordHash").doesNotExist());
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/contents").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsExpiredTokens() throws Exception {
        String token = login();
        String hash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        jdbc.update("UPDATE auth_tokens SET expires_at = ? WHERE token_hash = ?", java.sql.Timestamp.from(Instant.now().minusSeconds(10)), hash);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void filtersSearchesAndPaginatesContent() throws Exception {
        String header = "Bearer " + login();
        mvc.perform(get("/api/contents").header("Authorization", header).param("size", "2").param("page", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("items.length()").value(2))
                .andExpect(jsonPath("items[0].title").value("前后端协作指南"))
                .andExpect(jsonPath("total").value(8)).andExpect(jsonPath("stats.published").value(5))
                .andExpect(jsonPath("stats.draft").value(3));
        mvc.perform(get("/api/contents").header("Authorization", header).param("status", "DRAFT"))
                .andExpect(status().isOk()).andExpect(jsonPath("total").value(3))
                .andExpect(jsonPath("items[0].status").value("DRAFT"));
        mvc.perform(get("/api/contents").header("Authorization", header).param("q", "前后端"))
                .andExpect(status().isOk()).andExpect(jsonPath("total").value(1));
        mvc.perform(get("/api/contents").header("Authorization", header).param("q", "%"))
                .andExpect(status().isOk()).andExpect(jsonPath("total").value(0));
        mvc.perform(get("/api/contents").header("Authorization", header).param("size", "101"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/contents").header("Authorization", header).param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void corsAllowsOnlyConfiguredOrigins() throws Exception {
        mvc.perform(options("/api/auth/login").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/auth/login").header("Origin", "https://untrusted.example")
                .header("Access-Control-Request-Method", "POST")).andExpect(status().isForbidden());
    }
}
