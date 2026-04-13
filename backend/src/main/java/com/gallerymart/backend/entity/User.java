package com.gallerymart.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User entity — serves as both the JPA entity and Spring Security principal.
 * Roles are stored as a comma-separated string (e.g. "BUYER" or "BUYER,SELLER")
 * to support multi-role users without a join table.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 500)
    private String avatarUrl;

    /**
     * Comma-separated role string. E.g. "BUYER" or "BUYER,SELLER".
     * Parsed into GrantedAuthority list by getAuthorities().
     */
    @Column(nullable = false, length = 100)
    @Builder.Default
    private String roles = "BUYER";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ─── Spring Security UserDetails ───────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    public boolean hasRole(String role) {
        return Arrays.asList(roles.split(",")).contains(role.trim());
    }

    public void addRole(String role) {
        if (!hasRole(role)) {
            this.roles = this.roles + "," + role;
        }
    }
}
