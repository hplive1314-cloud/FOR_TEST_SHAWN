package com.qicheng.workspace.auth;

import com.qicheng.workspace.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected AuthToken() {}
    public AuthToken(String tokenHash, AppUser user, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
    }
    public AppUser getUser() { return user; }
    public Instant getExpiresAt() { return expiresAt; }
}
