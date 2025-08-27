package org.example.controller;

import org.example.service.BillingService;
import org.example.dao.BillDAO;
import org.example.model.Bill;
import org.example.model.TransactionType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Controller for bill management operations
 */
public class BillManagementController {
    private final BillingService billingService;
    private final BillDAO billDAO;
    private final Scanner scanner;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public BillManagementController(BillingService billingService, BillDAO billDAO, Scanner scanner) {
        this.billingService = billingService;
        this.billDAO = billDAO;
        this.scanner = scanner;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("              BILL MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1. View Bill by ID");
            System.out.println("2. View Bill by Serial Number");
            System.out.println("3. View Bills by Date");
            System.out.println("4. View Bills by Customer");
            System.out.println("5. View Bills by Transaction Type");
            System.out.println("6. List All Saved Bill Files");
            System.out.println("7. Back to Main Menu");
            System.out.println("=".repeat(50));
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewBillById();
                case "2" -> viewBillBySerialNumber();
                case "3" -> viewBillsByDate();
                case "4" -> viewBillsByCustomer();
                case "5" -> viewBillsByTransactionType();
                case "6" -> listSavedBillFiles();
                case "7" -> {
                    return;
                }
                default -> System.out.println("✗ Invalid choice. Please try again.");
            }
        }
    }

    private void viewBillById() {
        System.out.print("Enter Bill ID: ");
        String input = scanner.nextLine().trim();

        try {
            int billId = Integer.parseInt(input);
            Optional<Bill> billOpt = billDAO.getBillById(billId);

            if (billOpt.isPresent()) {
                displayBillDetails(billOpt.get());
            } else {
                System.out.println("✗ Bill not found with ID: " + billId);
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid Bill ID format.");
        }
    }

    private void viewBillBySerialNumber() {
        System.out.print("Enter Bill Serial Number: ");
        String input = scanner.nextLine().trim();

        try {
            int serialNumber = Integer.parseInt(input);
            Optional<Bill> billOpt = billDAO.getBillBySerialNumber(serialNumber);

            if (billOpt.isPresent()) {
                displayBillDetails(billOpt.get());
            } else {
                System.out.println("✗ Bill not found with Serial Number: " + serialNumber);
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid Serial Number format.");
        }
    }

    private void viewBillsByDate() {
        System.out.print("Enter date (dd/MM/yyyy): ");
        String dateInput = scanner.nextLine().trim();

        try {
            Date date = dateFormat.parse(dateInput);
            List<Bill> bills = billDAO.getBillsByDate(date);

            if (bills.isEmpty()) {
                System.out.println("No bills found for date: " + dateInput);
            } else {
                displayBillList(bills, "Bills for " + dateInput);
            }
        } catch (Exception e) {
            System.out.println("✗ Invalid date format. Use dd/MM/yyyy");
        }
    }

    private void viewBillsByCustomer() {
        System.out.print("Enter Customer ID: ");
        String input = scanner.nextLine().trim();

        try {
            int customerId = Integer.parseInt(input);
            List<Bill> bills = billDAO.getBillsByCustomer(customerId);

            if (bills.isEmpty()) {
                System.out.println("No bills found for Customer ID: " + customerId);
            } else {
                displayBillList(bills, "Bills for Customer ID: " + customerId);
            }
        } catch (NumberFormatException e) {
            System.out.println("✗ Invalid Customer ID format.");
        }
    }

    private void viewBillsByTransactionType() {
        System.out.println("Select Transaction Type:");
        System.out.println("1. IN_STORE");
        System.out.println("2. ONLINE");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();
        TransactionType transactionType = null;

        switch (choice) {
            case "1" -> transactionType = TransactionType.IN_STORE;
            case "2" -> transactionType = TransactionType.ONLINE;
            default -> {
                System.out.println("✗ Invalid choice.");
                return;
            }
        }

        List<Bill> bills = billDAO.getBillsByTransactionType(transactionType);

        if (bills.isEmpty()) {
            System.out.println("No bills found for transaction type: " + transactionType.getValue());
        } else {
            displayBillList(bills, "Bills for " + transactionType.getValue());
        }
    }

    private void listSavedBillFiles() {
        billingService.listSavedBills();
    }

    private void displayBillDetails(Bill bill) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("              BILL DETAILS");
        System.out.println("=".repeat(50));

        // Display the full bill template
        billingService.displayBillTemplate(bill.getBillId());

        // Show file location
        String filePath = billingService.getBillFilePath(bill.getBillId());
        if (filePath != null) {
            System.out.println("\nBill file location: " + filePath);
        }

        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void displayBillList(List<Bill> bills, String title) {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("                                   " + title.toUpperCase());
        System.out.println("=".repeat(100));

        System.out.printf("%-8s %-8s %-12s %-20s %10s %10s %12s%n",
                "BillID", "Serial", "Date", "Customer", "Total", "Type", "Status");
        System.out.println("-".repeat(100));

        for (Bill bill : bills) {
            System.out.printf("%-8d %-8d %tF %-20s Rs. %6.2f %-10s %12s%n",
                    bill.getBillId(),
                    bill.getSerialNumber(),
                    bill.getBillDate(),
                    "Customer " + bill.getCustomerId(), // Could enhance to show actual customer name
                    bill.getTotalAmount(),
                    bill.getTransactionType().getValue(),
                    bill.getStatus().toString());
        }

        System.out.println("-".repeat(100));
        System.out.println("Total bills: " + bills.size());

        // Ask if user wants to view details of a specific bill
        System.out.print("\nEnter Bill ID to view details (or press Enter to continue): ");
        String input = scanner.nextLine().trim();

        if (!input.isEmpty()) {
            try {
                int billId = Integer.parseInt(input);
                Optional<Bill> selectedBill = bills.stream()
                        .filter(bill -> bill.getBillId() == billId)
                        .findFirst();

                if (selectedBill.isPresent()) {
                    displayBillDetails(selectedBill.get());
                } else {
                    System.out.println("✗ Bill ID not found in the list.");
                }
            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid Bill ID format.");
            }
        }
    }
}
