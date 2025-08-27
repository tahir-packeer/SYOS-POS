// File: src/main/java/org/example/controller/AuthenticationController.java
package org.example.controller;

import org.example.model.User;
import org.example.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Controller for authentication operations
 */
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Handle user login
     */
    public boolean login(Scanner scanner) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("           LOGIN TO SYOS");
        System.out.println("=".repeat(40));

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return false;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (authService.login(username, password)) {
            User user = authService.getCurrentUser();
            System.out.println("\n✓ Login successful!");
            System.out.printf("Welcome, %s (%s)%n", user.getUsername(), user.getRole());
            return true;
        } else {
            System.out.println("\n✗ Invalid username or password.");
            return false;
        }
    }

    /**
     * Handle user logout
     */
    public void logout() {
        if (authService.isAuthenticated()) {
            User user = authService.getCurrentUser();
            authService.logout();
            System.out.printf("Goodbye, %s!%n", user.getUsername());
        }
    }

    /**
     * Create new user (admin only)
     */
    public void createUser(Scanner scanner) {
        if (!authService.canPerformAction("USER_MANAGEMENT")) {
            System.out.println("Access denied. Admin privileges required.");
            return;
        }

        System.out.println("\n--- Create New User ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.println("Available roles: CASHIER, MANAGER, ADMIN, CUSTOMER");
        System.out.print("Role: ");
        String roleStr = scanner.nextLine().trim().toUpperCase();

        try {
            User.UserRole role = User.UserRole.valueOf(roleStr);
            if (authService.createUser(username, password, role, email)) {
                System.out.println("✓ User created successfully!");
            } else {
                System.out.println("✗ Failed to create user. Username might already exist.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("✗ Invalid role specified.");
        }
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }

    /**
     * Get current user
     */
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    /**
     * Check if user can perform action
     */
    public boolean canPerformAction(String action) {
        return authService.canPerformAction(action);
    }
}