// File: src/main/java/org/example/view/CashierView.java
package org.example.view;

import org.example.controller.BillingController;
import org.example.controller.AuthenticationController;
import org.example.controller.BillManagementController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * View for cashier operations
 */
public class CashierView {
    private static final Logger logger = LoggerFactory.getLogger(CashierView.class);
    private BillingController billingController;
    private AuthenticationController authController;
    private BillManagementController billManagementController;

    public CashierView(BillingController billingController, AuthenticationController authController,
            BillManagementController billManagementController) {
        this.billingController = billingController;
        this.authController = authController;
        this.billManagementController = billManagementController;
    }

    /**
     * Start cashier interface
     */
    public boolean start(Scanner scanner) {
        while (true) {
            System.out.println("\n" + "=".repeat(40));
            System.out.println("           CASHIER MENU");
            System.out.println("=".repeat(40));
            System.out.println("1. Process In-Store Sale");
            System.out.println("2. Process Online Sale");
            System.out.println("3. Bill Management");
            System.out.println("9. Logout");
            System.out.println("0. Exit Application");
            System.out.println("=".repeat(40));

            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        billingController.startInStoreBilling(scanner);
                        break;
                    case "2":
                        billingController.startOnlineBilling(scanner);
                        break;
                    case "3":
                        billManagementController.showMenu();
                        break;
                    case "9":
                        authController.logout();
                        return true; // Return to login
                    case "0":
                        return false; // Exit application
                    default:
                        System.out.println("✗ Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("✗ An error occurred: " + e.getMessage());
                logger.error("Error in cashier view", e);
            }

            // Pause after operations (except logout/exit)
            if (!choice.equals("9") && !choice.equals("0")) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }
}