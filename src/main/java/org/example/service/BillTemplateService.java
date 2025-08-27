package org.example.service;

import org.example.dao.BillDAO;
import org.example.dao.CustomerDAO;
import org.example.model.Bill;
import org.example.model.BillItem;
import org.example.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Service for generating bill templates and saving them as files
 */
public class BillTemplateService {
    private static final Logger logger = LoggerFactory.getLogger(BillTemplateService.class);

    private final BillDAO billDAO;
    private final CustomerDAO customerDAO;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    // Create bills directory if it doesn't exist
    private static final String BILLS_DIRECTORY = "bills";

    public BillTemplateService(BillDAO billDAO, CustomerDAO customerDAO) {
        this.billDAO = billDAO;
        this.customerDAO = customerDAO;
        createBillsDirectory();
    }

    private void createBillsDirectory() {
        try {
            Path billsPath = Paths.get(BILLS_DIRECTORY);
            if (!Files.exists(billsPath)) {
                Files.createDirectories(billsPath);
                logger.info("Created bills directory: {}", billsPath.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create bills directory", e);
        }
    }

    /**
     * Generate and save bill template for a specific bill ID
     */
    public boolean generateAndSaveBill(int billId) {
        Optional<Bill> billOpt = billDAO.getBillById(billId);
        if (billOpt.isEmpty()) {
            logger.error("Bill not found with ID: {}", billId);
            return false;
        }

        Bill bill = billOpt.get();
        String billContent = generateBillTemplate(bill);
        return saveBillToFile(bill, billContent);
    }

    /**
     * Generate bill template content
     */
    public String generateBillTemplate(Bill bill) {
        Optional<Customer> customerOpt = customerDAO.getCustomerById(bill.getCustomerId());
        String customerName = customerOpt.map(Customer::getName).orElse("Unknown Customer");
        String customerPhone = customerOpt.map(Customer::getPhone).orElse("N/A");

        StringBuilder template = new StringBuilder();

        // Header
        template.append("=".repeat(50)).append("\n");
        template.append("              SYOS - SYNEX OUTLET STORE\n");
        template.append("=".repeat(50)).append("\n");
        template.append("              OFFICIAL RECEIPT\n");
        template.append("=".repeat(50)).append("\n\n");

        // Bill Information
        template.append("Bill ID: ").append(bill.getBillId()).append("\n");
        template.append("Serial No: ").append(bill.getSerialNumber()).append("\n");
        template.append("Date: ").append(dateFormat.format(bill.getBillDate())).append("\n");
        template.append("Transaction Type: ").append(bill.getTransactionType().getValue()).append("\n");
        template.append("Status: ").append(bill.getStatus().toString()).append("\n\n");

        // Customer Information
        template.append("Customer Details:\n");
        template.append("-".repeat(30)).append("\n");
        template.append("Name: ").append(customerName).append("\n");
        template.append("Phone: ").append(customerPhone).append("\n\n");

        // Items Table
        template.append("Items Purchased:\n");
        template.append("-".repeat(50)).append("\n");
        template.append(String.format("%-8s %-20s %8s %10s %10s\n",
                "Code", "Item Name", "Qty", "Price", "Total"));
        template.append("-".repeat(50)).append("\n");

        for (BillItem item : bill.getItems()) {
            template.append(String.format("%-8s %-20s %8d Rs. %6.2f Rs. %6.2f\n",
                    item.getItemCode(),
                    truncate(item.getItemName(), 20),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()));
        }

        template.append("-".repeat(50)).append("\n");

        // Totals
        double subtotal = bill.getTotalAmount() + bill.getDiscount();
        template.append(String.format("%-38s Rs. %6.2f\n", "Subtotal:", subtotal));

        if (bill.getDiscount() > 0) {
            double discountPercentage = (bill.getDiscount() / subtotal) * 100;
            template.append(
                    String.format("%-38s Rs. %6.2f (%.1f%%)\n", "Discount:", bill.getDiscount(), discountPercentage));
        } else {
            template.append(String.format("%-38s Rs. %6.2f\n", "Discount:", bill.getDiscount()));
        }

        template.append(String.format("%-38s Rs. %6.2f\n", "Total Amount:", bill.getTotalAmount()));
        template.append(String.format("%-38s Rs. %6.2f\n", "Cash Received:", bill.getCashReceived()));
        template.append(String.format("%-38s Rs. %6.2f\n", "Change:", bill.getChangeAmount()));
        template.append("-".repeat(50)).append("\n\n");

        // Footer
        template.append("Thank you for shopping with SYOS!\n");
        template.append("Please keep this receipt for your records.\n");
        template.append("For any queries, please contact us.\n\n");
        template.append("Generated on: ").append(dateFormat.format(new Date())).append("\n");
        template.append("=".repeat(50)).append("\n");

        return template.toString();
    }

    /**
     * Save bill content to file
     */
    private boolean saveBillToFile(Bill bill, String content) {
        try {
            String fileName = String.format("Bill_%d_Serial_%d_%s.txt",
                    bill.getBillId(),
                    bill.getSerialNumber(),
                    fileDateFormat.format(bill.getBillDate()));

            Path filePath = Paths.get(BILLS_DIRECTORY, fileName);

            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(content);
            }

            logger.info("Bill saved to file: {}", filePath.toAbsolutePath());
            return true;

        } catch (IOException e) {
            logger.error("Failed to save bill to file", e);
            return false;
        }
    }

    /**
     * Display bill template in console
     */
    public void displayBillTemplate(Bill bill) {
        String template = generateBillTemplate(bill);
        System.out.println(template);
    }

    /**
     * Get bill file path for a specific bill
     */
    public String getBillFilePath(int billId) {
        Optional<Bill> billOpt = billDAO.getBillById(billId);
        if (billOpt.isEmpty()) {
            return null;
        }

        Bill bill = billOpt.get();
        String fileName = String.format("Bill_%d_Serial_%d_%s.txt",
                bill.getBillId(),
                bill.getSerialNumber(),
                fileDateFormat.format(bill.getBillDate()));

        return Paths.get(BILLS_DIRECTORY, fileName).toAbsolutePath().toString();
    }

    /**
     * List all saved bill files
     */
    public void listSavedBills() {
        try {
            Path billsPath = Paths.get(BILLS_DIRECTORY);
            if (!Files.exists(billsPath)) {
                System.out.println("No bills directory found.");
                return;
            }

            File[] files = billsPath.toFile().listFiles((dir, name) -> name.endsWith(".txt"));
            if (files == null || files.length == 0) {
                System.out.println("No saved bills found.");
                return;
            }

            System.out.println("\nSaved Bills:");
            System.out.println("-".repeat(80));
            System.out.printf("%-20s %-20s %-20s %s\n", "Bill ID", "Serial No", "Date", "File Name");
            System.out.println("-".repeat(80));

            for (File file : files) {
                String fileName = file.getName();
                // Extract bill info from filename: Bill_123_Serial_456_2024-01-01_12-30-45.txt
                String[] parts = fileName.replace(".txt", "").split("_");
                if (parts.length >= 5) {
                    String billId = parts[1];
                    String serialNo = parts[3];
                    String date = parts[4] + " " + parts[5].replace("-", ":");
                    System.out.printf("%-20s %-20s %-20s %s\n", billId, serialNo, date, fileName);
                }
            }
            System.out.println("-".repeat(80));

        } catch (Exception e) {
            logger.error("Error listing saved bills", e);
            System.out.println("Error listing saved bills.");
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}
