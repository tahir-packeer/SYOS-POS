// File: src/main/java/org/example/view/ManagerView.java
package org.example.view;

import org.example.controller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * View for manager operations
 */
public class ManagerView {
    private static final Logger logger = LoggerFactory.getLogger(ManagerView.class);
    private BillingController billingController;
    private StockController stockController;
    private ReportController reportController;
    private ItemController itemController;
    private AuthenticationController authController;
    private BillManagementController billManagementController;

    public ManagerView(BillingController billingController, StockController stockController,
            ReportController reportController, ItemController itemController,
            AuthenticationController authController, BillManagementController billManagementController) {
        this.billingController = billingController;
        this.stockController = stockController;
        this.reportController = reportController;
        this.itemController = itemController;
        this.authController = authController;
        this.billManagementController = billManagementController;
    }

    /**
     * Start manager interface
     */
    public boolean start(Scanner scanner) {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("                MANAGER MENU");
            System.out.println("=".repeat(50));
            System.out.println("1. Process In-Store Sale");
            System.out.println("2. Process Online Sale");
            System.out.println("3. Stock Management");
            System.out.println("4. Item Management");
            System.out.println("5. Reports");
            System.out.println("6. Bill Management");
            System.out.println("9. Logout");
            System.out.println("0. Exit Application");
            System.out.println("=".repeat(50));

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
                        showStockManagementMenu(scanner);
                        break;
                    case "4":
                        itemController.showMenu(scanner);
                        break;
                    case "5":
                        reportController.showMenu(scanner);
                        break;
                    case "6":
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
                logger.error("Error in manager view", e);
            }

            // Pause after operations (except logout/exit/submenus)
            if (!choice.equals("9") && !choice.equals("0") &&
                    !choice.equals("3") && !choice.equals("4") && !choice.equals("5")) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private void showStockManagementMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("             STOCK MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1. Receive New Stock");
            System.out.println("2. Reshelve Items");
            System.out.println("3. Transfer Stock (Shelf ↔ Website)");
            System.out.println("4. View Stock Summary");
            System.out.println("5. View Expiring Stock");
            System.out.println("0. Back to Manager Menu");
            System.out.println("=".repeat(50));

            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        stockController.receiveStock(scanner);
                        break;
                    case "2":
                        stockController.reshelveItem(scanner);
                        break;
                    case "3":
                        stockController.transferStock(scanner);
                        break;
                    case "4":
                        stockController.viewStockSummary(scanner);
                        break;
                    case "5":
                        stockController.viewExpiringStock(scanner);
                        break;
                    case "0":
                        return; // Back to manager menu
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