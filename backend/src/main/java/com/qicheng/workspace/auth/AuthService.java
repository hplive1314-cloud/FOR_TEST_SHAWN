package com.qicheng.workspace.auth;

import com.qicheng.workspace.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final TokenRepository tokens;
    private final PasswordEncoder encoder;
    private final long tokenHours;
    private final SecureRandom random = new SecureRandom();
    private final String dummyHash;

    public AuthService(UserRepository users, TokenRepository tokens, PasswordEncoder encoder,
                       @Value("${app.token-hours}") long tokenHours) {
        if (tokenHours < 1 || tokenHours > 168) throw new IllegalArgumentException("Token lifetime must be 1–168 hours");
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.tokenHours = tokenHours;
        this.dummyHash = encoder.encode("not-a-real-user-password");
    }

    @Transactional
    public LoginResult login(String username, String password) {
        var user = users.findByUsername(username);
        boolean matches = encoder.matches(password, user.map(u -> u.getPasswordHash()).orElse(dummyHash));
        if (user.isEmpty() || !matches) throw new BadCredentialsException("用户名或密码错误");
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = Instant.now().plus(tokenHours, ChronoUnit.HOURS);
        tokens.deleteByExpiresAtBefore(Instant.now());
        tokens.save(new AuthToken(hash(token), user.get(), expiresAt));
        return new LoginResult(token, expiresAt, AppPrincipal.from(user.get()));
    }

    @Transactional(readOnly = true)
    public Optional<AppPrincipal> authenticate(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return Optional.empty();
        return tokens.findByTokenHash(hash(token))
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .map(t -> AppPrincipal.from(t.getUser()));
    }

    @Transactional
    public void logout(String token) {
        tokens.deleteByTokenHash(hash(token));
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record LoginResult(String token, Instant expiresAt, AppPrincipal user) {}
}
