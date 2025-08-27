// File: src/main/java/org/example/controller/StockController.java
package org.example.controller;

import org.example.model.StockBatch;
import org.example.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Enhanced stock controller with comprehensive stock management
 */
public class StockController {
    private static final Logger logger = LoggerFactory.getLogger(StockController.class);
    private StockService stockService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * Receive new stock batch
     */
    public void receiveStock(Scanner scanner) {
        System.out.println("\n--- Receive New Stock Batch ---");

        try {
            System.out.print("Item code: ");
            String code = scanner.nextLine().trim();

            if (code.isEmpty()) {
                System.out.println("✗ Item code cannot be empty.");
                return;
            }

            System.out.print("Quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            if (quantity <= 0) {
                System.out.println("✗ Quantity must be positive.");
                return;
            }

            System.out.print("Received date (yyyy-MM-dd): ");
            Date receivedDate = dateFormat.parse(scanner.nextLine().trim());

            System.out.print("Expiry date (yyyy-MM-dd): ");
            Date expiryDate = dateFormat.parse(scanner.nextLine().trim());

            if (expiryDate.before(receivedDate)) {
                System.out.println("✗ Expiry date cannot be before received date.");
                return;
            }

            System.out.print("Supplier name (optional): ");
            String supplier = scanner.nextLine().trim();
            if (supplier.isEmpty()) supplier = null;

            System.out.print("Purchase price per unit (optional): ");
            String priceStr = scanner.nextLine().trim();
            double purchasePrice = 0.0;
            if (!priceStr.isEmpty()) {
                try {
                    purchasePrice = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price format, setting to 0.00");
                }
            }

            StockBatch batch = new StockBatch(0, code, quantity, receivedDate, expiryDate, supplier, purchasePrice);

            if (stockService.receiveNewBatch(batch)) {
                System.out.println("✓ Stock batch received successfully!");
                System.out.printf("Added %d units of %s%n", quantity, code);
            } else {
                System.out.println("✗ Failed to receive stock batch.");
            }

        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid number format.");
        } catch (ParseException e) {
            System.out.println("✗ Invalid date format. Use yyyy-MM-dd");
        } catch (Exception e) {
            System.out.println("✗ Error receiving stock: " + e.getMessage());
            logger.error("Error receiving stock", e);
        }
    }

    /**
     * Reshelve items from warehouse to shelf
     */
    public void reshelveItem(Scanner scanner) {
        System.out.println("\n--- Reshelve Items ---");
        System.out.print("Item code to reshelve: ");
        String code = scanner.nextLine().trim();

        if (code.isEmpty()) {
            System.out.println("✗ Item code cannot be empty.");
            return;
        }

        if (stockService.reshelveItems(code)) {
            System.out.printf("✓ Items reshelved successfully for %s%n", code);
        } else {
            System.out.printf("✗ No items available for reshelving for %s%n", code);
        }
    }

    /**
     * Transfer stock between shelf and website
     */
    public void transferStock(Scanner scanner) {
        System.out.println("\n--- Transfer Stock ---");

        try {
            System.out.print("Item code: ");
            String code = scanner.nextLine().trim();

            if (code.isEmpty()) {
                System.out.println("✗ Item code cannot be empty.");
                return;
            }

            System.out.print("Quantity to transfer: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            if (quantity <= 0) {
                System.out.println("✗ Quantity must be positive.");
                return;
            }

            System.out.println("Transfer direction:");
            System.out.println("1. Shelf to Website");
            System.out.println("2. Website to Shelf");
            System.out.print("Choice: ");

            int choice = Integer.parseInt(scanner.nextLine().trim());
            boolean fromShelfToWebsite = (choice == 1);

            if (choice != 1 && choice != 2) {
                System.out.println("✗ Invalid choice.");
                return;
            }

            if (stockService.transferStock(code, quantity, fromShelfToWebsite)) {
                String direction = fromShelfToWebsite ? "shelf to website" : "website to shelf";
                System.out.printf("✓ Transferred %d units of %s from %s%n", quantity, code, direction);
            } else {
                System.out.println("✗ Transfer failed. Check stock availability.");
            }

        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid number format.");
        } catch (Exception e) {
            System.out.println("✗ Error during transfer: " + e.getMessage());
            logger.error("Error transferring stock", e);
        }
    }

    /**
     * View stock summary for an item
     */
    public void viewStockSummary(Scanner scanner) {
        System.out.println("\n--- Stock Summary ---");
        System.out.print("Item code: ");
        String code = scanner.nextLine().trim();

        if (code.isEmpty()) {
            System.out.println("✗ Item code cannot be empty.");
            return;
        }

        try {
            StockService.StockSummary summary = stockService.getStockSummary(code);

            System.out.printf("\nStock Summary for: %s%n", code);
            System.out.println("-".repeat(40));
            System.out.printf("Shelf Stock      : %d units%n", summary.getShelfQuantity());
            System.out.printf("Website Inventory: %d units%n", summary.getWebsiteQuantity());
            System.out.printf("Warehouse Stock  : %d units%n", summary.getWarehouseQuantity());
            System.out.printf("Total Stock      : %d units%n", summary.getTotalQuantity());
            System.out.printf("Batches          : %d%n", summary.getBatches().size());
            System.out.println("-".repeat(40));

        } catch (Exception e) {
            System.out.println("✗ Error retrieving stock summary: " + e.getMessage());
            logger.error("Error getting stock summary", e);
        }
    }

    /**
     * View expiring stock
     */
    public void viewExpiringStock(Scanner scanner) {
        System.out.println("\n--- Expiring Stock Report ---");
        System.out.print("Days from now (default 30): ");
        String daysStr = scanner.nextLine().trim();

        int days = 30;
        if (!daysStr.isEmpty()) {
            try {
                days = Integer.parseInt(daysStr);
                if (days <= 0) {
                    System.out.println("Using default 30 days.");
                    days = 30;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, using default 30 days.");
            }
        }

        List<StockBatch> expiringBatches = stockService.getBatchesExpiringSoon(days);

        if (expiringBatches.isEmpty()) {
            System.out.printf("No stock expiring within %d days.%n", days);
            return;
        }

        System.out.printf("\nStock expiring within %d days:%n", days);
        System.out.println("-".repeat(70));
        System.out.printf("%-12s %-12s %-12s %8s %-15s%n",
                "Item Code", "Batch ID", "Expiry Date", "Quantity", "Supplier");
        System.out.println("-".repeat(70));

        for (StockBatch batch : expiringBatches) {
            System.out.printf("%-12s %-12d %tF %8d %-15s%n",
                    batch.getItemCode(),
                    batch.getBatchId(),
                    batch.getExpiryDate(),
                    batch.getQuantity(),
                    batch.getSupplierName() != null ? batch.getSupplierName() : "N/A");
        }
        System.out.println("-".repeat(70));
    }
}