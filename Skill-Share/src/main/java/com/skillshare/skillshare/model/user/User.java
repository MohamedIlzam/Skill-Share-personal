package com.skillshare.skillshare.model.user;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email")
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Long auto-increment
    private Long id;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING) // store "USER"/"ADMIN" as String
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // JPA only
    protected User() {}

    private User(String fullName, String email, String passwordHash, Role role) {
        this.fullName = requireNonBlank(fullName, "fullName");
        this.email = requireNonBlank(email, "email").toLowerCase();
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        this.role = Objects.requireNonNull(role, "role");
    }

    /** Factory method for registration */
    public static User register(String fullName, String email, String passwordHash) {
        return new User(fullName, email, passwordHash, Role.USER);
    }

    /** Example controlled change (SOLID-friendly) */
    public void promoteToAdmin() {
        this.role = Role.ADMIN;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    // Getters only (keep entity safe)
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
