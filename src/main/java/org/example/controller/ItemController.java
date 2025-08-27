// File: src/main/java/org/example/controller/ItemController.java
package org.example.controller;

import org.example.model.Item;
import org.example.dao.ItemDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Controller for item management operations
 */
public class ItemController {
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private ItemDAO itemDAO;

    public ItemController(ItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    /**
     * Show item management menu
     */
    public void showMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("              ITEM MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1. Add New Item");
            System.out.println("2. View Item Details");
            System.out.println("3. Update Item");
            System.out.println("4. List All Items");
            System.out.println("5. Deactivate Item");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(50));

            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addItem(scanner);
                    case "2" -> viewItem(scanner);
                    case "3" -> updateItem(scanner);
                    case "4" -> listAllItems();
                    case "5" -> deactivateItem(scanner);
                    case "0" -> {
                        System.out.println("Returning to main menu...");
                        return;
                    }
                    default -> System.out.println("✗ Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
                logger.error("Error in item management", e);
            }

            if (!choice.equals("0")) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private void addItem(Scanner scanner) {
        System.out.println("\n--- Add New Item ---");

        System.out.print("Item code: ");
        String code = scanner.nextLine().trim().toUpperCase();

        if (code.isEmpty()) {
            System.out.println("✗ Item code cannot be empty.");
            return;
        }

        if (itemDAO.isItemCodeExists(code)) {
            System.out.println("✗ Item code already exists: " + code);
            return;
        }

        System.out.print("Item name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("✗ Item name cannot be empty.");
            return;
        }

        System.out.print("Price: Rs. ");
        double price;
        try {
            price = Double.parseDouble(scanner.nextLine().trim());
            if (price < 0) {
                System.out.println("✗ Price cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid price format.");
            return;
        }

        System.out.print("Category: ");
        String category = scanner.nextLine().trim();

        if (category.isEmpty()) {
            category = "GENERAL";
        }

        System.out.print("Description (optional): ");
        String description = scanner.nextLine().trim();

        System.out.print("Minimum stock level (default 50): ");
        String minStockStr = scanner.nextLine().trim();
        int minStockLevel = 50;
        if (!minStockStr.isEmpty()) {
            try {
                minStockLevel = Integer.parseInt(minStockStr);
                if (minStockLevel < 0) minStockLevel = 50;
            } catch (NumberFormatException e) {
                System.out.println("Invalid stock level, using default 50.");
            }
        }

        Item item = new Item(code, name, price, category, description, minStockLevel);
        itemDAO.addItem(item);

        System.out.println("✓ Item added successfully!");
        System.out.printf("Code: %s, Name: %s, Price: Rs. %.2f%n", code, name, price);
    }

    private void viewItem(Scanner scanner) {
        System.out.println("\n--- View Item Details ---");
        System.out.print("Item code: ");
        String code = scanner.nextLine().trim().toUpperCase();

        Optional<Item> itemOpt = itemDAO.getItemByCode(code);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            System.out.println("\nItem Details:");
            System.out.println("-".repeat(40));
            System.out.printf("Code        : %s%n", item.getItemCode());
            System.out.printf("Name        : %s%n", item.getName());
            System.out.printf("Price       : Rs. %.2f%n", item.getPrice());
            System.out.printf("Category    : %s%n", item.getCategory());
            System.out.printf("Description : %s%n", item.getDescription() != null ? item.getDescription() : "N/A");
            System.out.printf("Min Stock   : %d units%n", item.getMinStockLevel());
            System.out.printf("Active      : %s%n", item.isActive() ? "Yes" : "No");
            System.out.println("-".repeat(40));
        } else {
            System.out.println("✗ Item not found: " + code);
        }
    }

    private void updateItem(Scanner scanner) {
        System.out.println("\n--- Update Item ---");
        System.out.print("Item code: ");
        String code = scanner.nextLine().trim().toUpperCase();

        Optional<Item> itemOpt = itemDAO.getItemByCode(code);
        if (itemOpt.isEmpty()) {
            System.out.println("✗ Item not found: " + code);
            return;
        }

        Item item = itemOpt.get();
        System.out.printf("Updating item: %s%n", item.getName());

        System.out.printf("Name (%s): ", item.getName());
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) {
            item.setName(name);
        }

        System.out.printf("Price (%.2f): Rs. ", item.getPrice());
        String priceStr = scanner.nextLine().trim();
        if (!priceStr.isEmpty()) {
            try {
                double price = Double.parseDouble(priceStr);
                if (price >= 0) {
                    item.setPrice(price);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid price, keeping current value.");
            }
        }

        System.out.printf("Category (%s): ", item.getCategory());
        String category = scanner.nextLine().trim();
        if (!category.isEmpty()) {
            item.setCategory(category);
        }

        if (itemDAO.updateItem(item)) {
            System.out.println("✓ Item updated successfully!");
        } else {
            System.out.println("✗ Failed to update item.");
        }
    }

    private void listAllItems() {
        System.out.println("\n--- All Items ---");
        List<Item> items = itemDAO.getAllItems();

        if (items.isEmpty()) {
            System.out.println("No items found.");
            return;
        }

        System.out.printf("%-12s %-25s %10s %-15s %8s%n",
                "Code", "Name", "Price", "Category", "Active");
        System.out.println("-".repeat(75));

        for (Item item : items) {
            System.out.printf("%-12s %-25s Rs. %6.2f %-15s %8s%n",
                    item.getItemCode(),
                    truncate(item.getName(), 25),
                    item.getPrice(),
                    item.getCategory(),
                    item.isActive() ? "Yes" : "No");
        }
    }

    private void deactivateItem(Scanner scanner) {
        System.out.println("\n--- Deactivate Item ---");
        System.out.print("Item code: ");
        String code = scanner.nextLine().trim().toUpperCase();

        Optional<Item> itemOpt = itemDAO.getItemByCode(code);
        if (itemOpt.isEmpty()) {
            System.out.println("✗ Item not found: " + code);
            return;
        }

        Item item = itemOpt.get();
        System.out.printf("Are you sure you want to deactivate '%s'? (y/N): ", item.getName());
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            if (itemDAO.deactivateItem(code)) {
                System.out.println("✓ Item deactivated successfully!");
            } else {
                System.out.println("✗ Failed to deactivate item.");
            }
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}