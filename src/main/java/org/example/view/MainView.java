// File: src/main/java/org/example/view/MainView.java
package org.example.view;

import org.example.controller.AuthenticationController;
import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Main application view with authentication and role-based navigation
 */
public class MainView {
    private static final Logger logger = LoggerFactory.getLogger(MainView.class);
    private AuthenticationController authController;
    private CashierView cashierView;
    private ManagerView managerView;
    private AdminView adminView;

    public MainView(AuthenticationController authController, CashierView cashierView,
                    ManagerView managerView, AdminView adminView) {
        this.authController = authController;
        this.cashierView = cashierView;
        this.managerView = managerView;
        this.adminView = adminView;
    }

    /**
     * Start the main application
     */
    public void start(Scanner scanner) {
        displayWelcome();

        while (true) {
            if (!authController.isAuthenticated()) {
                if (!handleLogin(scanner)) {
                    break; // User chose to exit
                }
            } else {
                if (!handleMainMenu(scanner)) {
                    break; // User chose to exit
                }
            }
        }

        if (authController.isAuthenticated()) {
            authController.logout();
        }

        System.out.println("\nThank you for using SYOS!");
        System.out.println("Application terminated.");
    }

    private void displayWelcome() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("    ███████ ██    ██  ██████  ███████ ");
        System.out.println("    ██       ██  ██  ██    ██ ██      ");
        System.out.println("    ███████   ████   ██    ██ ███████ ");
        System.out.println("         ██    ██    ██    ██      ██ ");
        System.out.println("    ███████    ██     ██████  ███████ ");
        System.out.println();
        System.out.println("           Synex Outlet Store System");
        System.out.println("              Version 1.0.0");
        System.out.println("=".repeat(60));
    }

    private boolean handleLogin(Scanner scanner) {
        while (true) {
            System.out.println("\n1. Login");
            System.out.println("0. Exit Application");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    if (authController.login(scanner)) {
                        return true; // Successfully logged in
                    }
                    // Login failed, continue loop
                    break;
                case "0":
                    return false; // Exit application
                default:
                    System.out.println("✗ Invalid choice. Please try again.");
            }
        }
    }

    private boolean handleMainMenu(Scanner scanner) {
        User currentUser = authController.getCurrentUser();

        System.out.println("\n" + "=".repeat(50));
        System.out.printf("Welcome, %s (%s)%n", currentUser.getUsername(), currentUser.getRole());
        System.out.println("=".repeat(50));

        switch (currentUser.getRole()) {
            case CASHIER:
                return cashierView.start(scanner);
            case MANAGER:
                return managerView.start(scanner);
            case ADMIN:
                return adminView.start(scanner);
            case CUSTOMER:
                System.out.println("Customer interface not implemented in console version.");
                authController.logout();
                return true;
            default:
                System.out.println("Unknown user role. Logging out...");
                authController.logout();
                return true;
        }
    }
}