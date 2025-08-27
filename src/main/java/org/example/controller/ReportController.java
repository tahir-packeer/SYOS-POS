// File: src/main/java/org/example/controller/ReportController.java
package org.example.controller;

import org.example.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Enhanced report controller with comprehensive reporting options
 */
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private ReportService reportService;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Show main report menu
     */
    public void showMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("                 REPORTS MENU");
            System.out.println("=".repeat(50));
            System.out.println("1. Daily Sales Report");
            System.out.println("2. Reorder Report");
            System.out.println("3. Stock Report");
            System.out.println("4. Bill History");
            System.out.println("5. Reshelving Report");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(50));

            System.out.print("Choose option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> showDailySalesReport(scanner);
                    case "2" -> reportService.printReorderReport();
                    case "3" -> reportService.printStockReport();
                    case "4" -> reportService.printBillHistory();
                    case "5" -> reportService.printReshelvingReport();
                    case "0" -> {
                        System.out.println("Returning to main menu...");
                        return;
                    }
                    default -> System.out.println("✗ Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("✗ Error generating report: " + e.getMessage());
                logger.error("Error in report generation", e);
            }

            if (!choice.equals("0")) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    /**
     * Show daily sales report with date input
     */
    private void showDailySalesReport(Scanner scanner) {
        System.out.println("\n--- Daily Sales Report ---");
        System.out.print("Enter date (yyyy-MM-dd) or press Enter for today: ");
        String dateStr = scanner.nextLine().trim();

        Date date;
        if (dateStr.isEmpty()) {
            date = new Date(); // Today
            System.out.println("Using today's date...");
        } else {
            try {
                date = dateFormat.parse(dateStr);
            } catch (ParseException e) {
                System.out.println("✗ Invalid date format. Using today's date.");
                date = new Date();
            }
        }

        reportService.printDailySalesReport(date);
    }
}