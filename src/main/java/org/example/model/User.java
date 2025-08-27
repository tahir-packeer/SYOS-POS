// File: src/main/java/org/example/model/User.java
package org.example.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private String email;
    private boolean isActive;
    private LocalDateTime createdAt;

    public enum UserRole {
        ADMIN, MANAGER, CASHIER, CUSTOMER
    }

    public User(int userId, String username, String passwordHash, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = UserRole.valueOf(role.toUpperCase());
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public User(int userId, String username, String passwordHash, UserRole role, String email) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public String getRoleString() { return role.toString(); }
    public String getEmail() { return email; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s}", userId, username, role);
    }
}