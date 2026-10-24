package com.qicheng.workspace.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true")
public class BootstrapData implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public BootstrapData(UserRepository users, PasswordEncoder encoder,
                         @Value("${app.bootstrap.username}") String username,
                         @Value("${app.bootstrap.password}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.findByUsername(username).isPresent()) return;
        if (username.isBlank() || username.length() > 64 || password.length() < 12
                || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Bootstrap requires a username and a password of at least 12 characters (at most 72 UTF-8 bytes)");
        }
        users.save(new AppUser(username, encoder.encode(password), "管理员"));
    }
}
