// File: src/main/java/org/example/controller/BillingController.java
package org.example.controller;

import org.example.model.*;
import org.example.service.BillingService;
import org.example.service.DiscountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Enhanced billing controller with comprehensive validation
 */
public class BillingController {
    private static final Logger logger = LoggerFactory.getLogger(BillingController.class);
    private BillingService billingService;
    private DiscountService discountService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
        this.discountService = new DiscountService();
    }

    /**
     * Start billing process for in-store transactions
     */
    public void startInStoreBilling(Scanner scanner) {
        processTransaction(scanner, TransactionType.IN_STORE);
    }

    /**
     * Start billing process for online transactions
     */
    public void startOnlineBilling(Scanner scanner) {
        processTransaction(scanner, TransactionType.ONLINE);
    }

    private List<BillItem> collectItems(Scanner scanner, TransactionType transactionType) {
        List<BillItem> items = new ArrayList<>();

        System.out.println("\nAdd items to bill (type 'done' to finish):");

        while (true) {
            System.out.print("Item code: ");
            String code = scanner.nextLine().trim();

            if (code.equalsIgnoreCase("done")) {
                break;
            }

            if (code.isEmpty()) {
                System.out.println("Item code cannot be empty.");
                continue;
            }

            // Validate item exists
            Optional<Item> itemOpt = billingService.getItemDetails(code);
            if (itemOpt.isEmpty()) {
                System.out.println("✗ Item not found: " + code);
                continue;
            }

            Item item = itemOpt.get();
            System.out.printf("Found: %s - Rs. %.2f%n", item.getName(), item.getPrice());

            System.out.print("Quantity: ");
            try {
                int quantity = Integer.parseInt(scanner.nextLine().trim());
                if (quantity <= 0) {
                    System.out.println("✗ Quantity must be positive.");
                    continue;
                }

                // Check availability
                if (!billingService.isItemAvailable(code, quantity, transactionType)) {
                    System.out.printf("✗ Insufficient stock. Available: %s inventory%n",
                            transactionType == TransactionType.IN_STORE ? "shelf" : "website");
                    continue;
                }

                BillItem billItem = new BillItem(code, item.getName(), quantity, item.getPrice());
                items.add(billItem);

                System.out.printf("✓ Added: %d x %s%n", quantity, item.getName());

            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid quantity entered.");
            }
        }

        return items;
    }

    private void displayBillSummary(List<BillItem> items) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("                    BILL SUMMARY");
        System.out.println("-".repeat(60));
        System.out.printf("%-12s %-20s %6s %10s %10s%n", "Code", "Item", "Qty", "Price", "Total");
        System.out.println("-".repeat(60));

        double grandTotal = 0;
        for (BillItem item : items) {
            double itemTotal = item.getQuantity() * item.getUnitPrice();
            grandTotal += itemTotal;

            System.out.printf("%-12s %-20s %6d Rs. %6.2f Rs. %6.2f%n",
                    item.getItemCode(),
                    truncate(item.getItemName(), 20),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    itemTotal);
        }

        System.out.println("-".repeat(60));
        System.out.printf("%-49s Rs. %6.2f%n", "SUBTOTAL", grandTotal);

        System.out.println("-".repeat(60));
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("\\d{10}");
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    private double getDiscountInput(Scanner scanner, double subtotal) {
        System.out.print("Enter discount amount (or press Enter for no discount): Rs. ");
        String discountInput = scanner.nextLine().trim();

        if (discountInput.isEmpty()) {
            return 0.0; // No discount
        }

        try {
            double discount = Double.parseDouble(discountInput);

            // Validate discount using service
            if (!discountService.isValidDiscount(discount, subtotal)) {
                System.out.println("✗ Invalid discount amount. No discount applied.");
                return 0.0;
            }

            return discount;

        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid discount amount. No discount applied.");
            return 0.0;
        }
    }

    private void processTransaction(Scanner scanner, TransactionType transactionType) {
        System.out.println("\n" + "=".repeat(50));
        System.out.printf("           %s BILLING%n", transactionType.getValue());
        System.out.println("=".repeat(50));

        // Collect items
        List<BillItem> billItems = collectItems(scanner, transactionType);
        if (billItems.isEmpty()) {
            System.out.println("✗ No valid items added to bill.");
            return;
        }

        // Display bill summary
        displayBillSummary(billItems);

        // Calculate subtotal
        double subtotal = billItems.stream()
                .mapToDouble(item -> item.getQuantity() * billingService.getItemPrice(item.getItemCode()))
                .sum();

        // Get discount
        double discount = getDiscountInput(scanner, subtotal);
        double totalAmount = subtotal - discount;

        // Debug logging
        logger.debug("\nDiscount entered: {}, \nSubtotal: {}, \nTotal Amount: {}", discount, subtotal, totalAmount);

        // Get customer information
        System.out.print("Customer phone number: ");
        String phone = scanner.nextLine().trim();

        if (!isValidPhoneNumber(phone)) {
            System.out.println("✗ Invalid phone number format. Must be 10 digits.");
            return;
        }

        System.out.print("Customer name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("✗ Customer name cannot be empty.");
            return;
        }

        // Get payment
        double cashReceived = 0;
        if (transactionType == TransactionType.IN_STORE) {
            System.out.printf("Subtotal: Rs. %.2f%n", subtotal);
            System.out.printf("Discount: Rs. %.2f%n", discount);
            System.out.printf("Total amount: Rs. %.2f%n", totalAmount);
            System.out.print("Cash received: Rs. ");

            try {
                cashReceived = Double.parseDouble(scanner.nextLine());
                if (cashReceived < totalAmount) {
                    System.out.println("✗ Insufficient cash received.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid amount entered.");
                return;
            }
        } else {
            cashReceived = totalAmount; // Online transactions are prepaid
        }

        // Process billing
        int billId = billingService.processBilling(phone, name, billItems, cashReceived, transactionType, discount);

        if (billId > 0) {
            System.out.println("\n✓ Bill processed successfully!");
            System.out.printf("Bill ID: %d%n", billId);

            if (transactionType == TransactionType.IN_STORE) {
                double change = cashReceived - totalAmount;
                System.out.printf("Change to return: Rs. %.2f%n", change);
            }

            // Display the bill template
            System.out.println("\n" + "=".repeat(50));
            System.out.println("              BILL RECEIPT");
            System.out.println("=".repeat(50));
            billingService.displayBillTemplate(billId);

            // Show file location
            String filePath = billingService.getBillFilePath(billId);
            if (filePath != null) {
                System.out.println("\nBill saved to: " + filePath);
            }

            System.out.println("Thank you for shopping with SYOS!");
        } else {
            System.out.println("✗ Failed to process bill. Please check inventory and try again.");
        }
    }
}
