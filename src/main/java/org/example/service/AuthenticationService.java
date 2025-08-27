// File: src/main/java/org/example/service/AuthenticationService.java
package org.example.service;

import org.example.dao.UserDAO;
import org.example.model.User;
import org.example.util.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service for user authentication and session management
 */
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private UserDAO userDAO;
    private User currentUser;

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticate user with username and password
     */
    public boolean login(String username, String password) {
        try {
            Optional<User> userOpt = userDAO.getUserByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.isActive() && PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
                    this.currentUser = user;
                    logger.info("User logged in successfully: {}", username);
                    return true;
                }
            }
            logger.warn("Failed login attempt for username: {}", username);
            return false;
        } catch (Exception e) {
            logger.error("Error during login for username: " + username, e);
            return false;
        }
    }

    /**
     * Logout current user
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
            this.currentUser = null;
        }
    }

    /**
     * Check if user is currently authenticated
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(User.UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    /**
     * Check if current user can perform action (authorization)
     */
    public boolean canPerformAction(String action) {
        if (currentUser == null) {
            return false;
        }

        return switch (action.toUpperCase()) {
            case "BILLING", "VIEW_ITEMS" ->
                    hasRole(User.UserRole.CASHIER) || hasRole(User.UserRole.MANAGER) || hasRole(User.UserRole.ADMIN);
            case "STOCK_MANAGEMENT", "VIEW_REPORTS", "MANAGE_ITEMS" ->
                    hasRole(User.UserRole.MANAGER) || hasRole(User.UserRole.ADMIN);
            case "USER_MANAGEMENT", "SYSTEM_CONFIG" ->
                    hasRole(User.UserRole.ADMIN);
            default -> false;
        };
    }

    /**
     * Create new user account (admin only)
     */
    public boolean createUser(String username, String password, User.UserRole role, String email) {
        if (!hasRole(User.UserRole.ADMIN)) {
            logger.warn("Unauthorized attempt to create user by: {}", currentUser != null ? currentUser.getUsername() : "unknown");
            return false;
        }

        if (userDAO.isUsernameExists(username)) {
            logger.warn("Username already exists: {}", username);
            return false;
        }

        String hashedPassword = PasswordUtils.hashPassword(password);
        User newUser = new User(0, username, hashedPassword, role, email);

        int userId = userDAO.addUser(newUser);
        if (userId > 0) {
            logger.info("New user created: {} with role: {}", username, role);
            return true;
        }

        return false;
    }
}

