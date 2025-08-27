// File: src/main/java/org/example/service/ReportService.java
package org.example.service;

import org.example.config.ConfigManager;
import org.example.dao.*;
import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced report service with comprehensive reporting capabilities
 */
public class ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final BillDAO billDAO;
    private final ItemDAO itemDAO;
    private final ShelfStockDAO shelfStockDAO;
    private final WebsiteInventoryDAO websiteInventoryDAO;
    private final StockBatchDAO stockBatchDAO;
    private final CustomerDAO customerDAO;
    private final ConfigManager config;

    public ReportService(BillDAO billDAO, ItemDAO itemDAO, ShelfStockDAO shelfStockDAO,
                         WebsiteInventoryDAO websiteInventoryDAO, StockBatchDAO stockBatchDAO,
                         CustomerDAO customerDAO) {
        this.billDAO = billDAO;
        this.itemDAO = itemDAO;
        this.shelfStockDAO = shelfStockDAO;
        this.websiteInventoryDAO = websiteInventoryDAO;
        this.stockBatchDAO = stockBatchDAO;
        this.customerDAO = customerDAO;
        this.config = ConfigManager.getInstance();
    }

    /**
     * Generate comprehensive daily sales report
     */
    public DailySalesReport generateDailySalesReport(Date date) {
        List<Bill> bills = billDAO.getBillsByDate(date);

        Map<String, ItemSalesData> itemSalesMap = new HashMap<>();
        double totalRevenue = 0.0;
        int totalTransactions = bills.size();
        int inStoreTransactions = 0;
        int onlineTransactions = 0;

        for (Bill bill : bills) {
            totalRevenue += bill.getTotalAmount();

            if (bill.getTransactionType() == TransactionType.IN_STORE) {
                inStoreTransactions++;
            } else {
                onlineTransactions++;
            }

            for (BillItem item : bill.getItems()) {
                String itemCode = item.getItemCode();
                itemSalesMap.computeIfAbsent(itemCode, k -> new ItemSalesData(itemCode, item.getItemName()))
                        .addSale(item.getQuantity(), item.getTotalPrice(), bill.getTransactionType());
            }
        }

        return new DailySalesReport(date, itemSalesMap.values(), totalRevenue,
                totalTransactions, inStoreTransactions, onlineTransactions);
    }

    /**
     * Print daily sales report to console
     */
    public void printDailySalesReport(Date date) {
        DailySalesReport report = generateDailySalesReport(date);

        System.out.println("\n" + "=".repeat(80));
        System.out.printf("                    DAILY SALES REPORT - %tF%n", date);
        System.out.println("=".repeat(80));

        System.out.printf("Total Transactions: %d (In-Store: %d, Online: %d)%n",
                report.getTotalTransactions(), report.getInStoreTransactions(), report.getOnlineTransactions());
        System.out.printf("Total Revenue: Rs. %.2f%n%n", report.getTotalRevenue());

        if (report.getItemSales().isEmpty()) {
            System.out.println("No sales recorded for this date.");
            return;
        }

        System.out.printf("%-12s %-25s %8s %8s %8s %12s%n",
                "Code", "Item Name", "In-Store", "Online", "Total", "Revenue");
        System.out.println("-".repeat(80));

        for (ItemSalesData itemSale : report.getItemSales()) {
            System.out.printf("%-12s %-25s %8d %8d %8d Rs. %8.2f%n",
                    itemSale.getItemCode(),
                    truncate(itemSale.getItemName(), 25),
                    itemSale.getInStoreQuantity(),
                    itemSale.getOnlineQuantity(),
                    itemSale.getTotalQuantity(),
                    itemSale.getTotalRevenue());
        }

        System.out.println("=".repeat(80));
    }

    /**
     * Generate and print reorder report
     */
    public void printReorderReport() {
        int threshold = config.getReorderThreshold();
        List<ShelfStock> lowShelfStock = shelfStockDAO.getLowStockItems(threshold);
        List<WebsiteInventory> lowWebsiteStock = websiteInventoryDAO.getAllWebsiteInventory()
                .stream()
                .filter(inv -> inv.getQuantity() < threshold)
                .collect(Collectors.toList());

        System.out.println("\n" + "=".repeat(70));
        System.out.printf("                    REORDER REPORT (Threshold: %d)%n", threshold);
        System.out.println("=".repeat(70));

        if (lowShelfStock.isEmpty() && lowWebsiteStock.isEmpty()) {
            System.out.println("All items are adequately stocked.");
            return;
        }

        // Shelf stock needing reorder
        if (!lowShelfStock.isEmpty()) {
            System.out.println("\nSHELF STOCK - Items needing reorder:");
            System.out.printf("%-12s %-30s %10s%n", "Code", "Item Name", "Quantity");
            System.out.println("-".repeat(54));

            for (ShelfStock stock : lowShelfStock) {
                Optional<Item> itemOpt = itemDAO.getItemByCode(stock.getItemCode());
                String itemName = itemOpt.map(Item::getName).orElse("Unknown Item");
                System.out.printf("%-12s %-30s %10d%n",
                        stock.getItemCode(), truncate(itemName, 30), stock.getQuantity());
            }
        }

        // Website inventory needing reorder
        if (!lowWebsiteStock.isEmpty()) {
            System.out.println("\nWEBSITE INVENTORY - Items needing reorder:");
            System.out.printf("%-12s %-30s %10s%n", "Code", "Item Name", "Quantity");
            System.out.println("-".repeat(54));

            for (WebsiteInventory inv : lowWebsiteStock) {
                Optional<Item> itemOpt = itemDAO.getItemByCode(inv.getItemCode());
                String itemName = itemOpt.map(Item::getName).orElse("Unknown Item");
                System.out.printf("%-12s %-30s %10d%n",
                        inv.getItemCode(), truncate(itemName, 30), inv.getQuantity());
            }
        }

        System.out.println("=".repeat(70));
    }

    /**
     * Generate and print comprehensive stock report
     */
    public void printStockReport() {
        List<StockBatch> batches = stockBatchDAO.getAllBatches();
        Map<String, List<StockBatch>> batchesByItem = batches.stream()
                .collect(Collectors.groupingBy(StockBatch::getItemCode));

        System.out.println("\n" + "=".repeat(90));
        System.out.println("                              STOCK REPORT");
        System.out.println("=".repeat(90));

        if (batchesByItem.isEmpty()) {
            System.out.println("No stock batches found.");
            return;
        }

        for (Map.Entry<String, List<StockBatch>> entry : batchesByItem.entrySet()) {
            String itemCode = entry.getKey();
            List<StockBatch> itemBatches = entry.getValue();

            Optional<Item> itemOpt = itemDAO.getItemByCode(itemCode);
            String itemName = itemOpt.map(Item::getName).orElse("Unknown Item");

            System.out.printf("\nItem: %s - %s%n", itemCode, itemName);
            System.out.printf("%-8s %-12s %-12s %8s %-15s %8s%n",
                    "BatchID", "Received", "Expiry", "Qty", "Supplier", "Status");
            System.out.println("-".repeat(70));

            for (StockBatch batch : itemBatches) {
                String status = batch.isMovedToShelf() ? "Shelved" : "Warehouse";
                if (batch.getQuantity() == 0) {
                    status = "Empty";
                }

                System.out.printf("%-8d %tF %tF %8d %-15s %8s%n",
                        batch.getBatchId(),
                        batch.getReceivedDate(),
                        batch.getExpiryDate(),
                        batch.getQuantity(),
                        truncate(batch.getSupplierName() != null ? batch.getSupplierName() : "N/A", 15),
                        status);
            }
        }

        System.out.println("=".repeat(90));
    }

    /**
     * Generate and print bill history report
     */
    public void printBillHistory() {
        List<Bill> bills = billDAO.getAllBills();

        System.out.println("\n" + "=".repeat(100));
        System.out.println("                                   BILL HISTORY");
        System.out.println("=".repeat(100));

        if (bills.isEmpty()) {
            System.out.println("No bills found.");
            return;
        }

        System.out.printf("%-8s %-8s %-12s %-20s %10s %10s %12s%n",
                "BillID", "Serial", "Date", "Customer", "Total", "Type", "Status");
        System.out.println("-".repeat(100));

        for (Bill bill : bills) {
            Optional<Customer> customerOpt = customerDAO.getCustomerById(bill.getCustomerId());
            String customerName = customerOpt.map(Customer::getName).orElse("Unknown");

            System.out.printf("%-8d %-8d %tF %-20s Rs. %6.2f %-10s %12s%n",
                    bill.getBillId(),
                    bill.getSerialNumber(),
                    bill.getBillDate(),
                    truncate(customerName, 20),
                    bill.getTotalAmount(),
                    bill.getTransactionType().getValue(),
                    bill.getStatus().toString());
        }

        System.out.println("=".repeat(100));
    }

    /**
     * Generate items to be reshelved report
     */
    public void printReshelvingReport() {
        List<Item> allItems = itemDAO.getActiveItems();
        List<ReshelvingData> reshelvingData = new ArrayList<>();

        for (Item item : allItems) {
            Optional<ShelfStock> shelfStockOpt = shelfStockDAO.getShelfStock(item.getItemCode());
            int shelfQty = shelfStockOpt.map(ShelfStock::getQuantity).orElse(0);

            List<StockBatch> availableBatches = stockBatchDAO.getBatchesForReshelving(item.getItemCode());
            int warehouseQty = availableBatches.stream()
                    .mapToInt(StockBatch::getQuantity)
                    .sum();

            if (warehouseQty > 0) {
                reshelvingData.add(new ReshelvingData(item.getItemCode(), item.getName(),
                        shelfQty, warehouseQty, availableBatches.size()));
            }
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("                          RESHELVING REPORT");
        System.out.println("=".repeat(80));

        if (reshelvingData.isEmpty()) {
            System.out.println("No items need reshelving at this time.");
            return;
        }

        System.out.printf("%-12s %-25s %10s %10s %8s%n",
                "Code", "Item Name", "Shelf Qty", "Warehouse", "Batches");
        System.out.println("-".repeat(80));

        for (ReshelvingData data : reshelvingData) {
            System.out.printf("%-12s %-25s %10d %10d %8d%n",
                    data.getItemCode(),
                    truncate(data.getItemName(), 25),
                    data.getShelfQuantity(),
                    data.getWarehouseQuantity(),
                    data.getBatchCount());
        }

        System.out.println("=".repeat(80));
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    // Inner classes for report data structures
    public static class DailySalesReport {
        private final Date date;
        private final Collection<ItemSalesData> itemSales;
        private final double totalRevenue;
        private final int totalTransactions;
        private final int inStoreTransactions;
        private final int onlineTransactions;

        public DailySalesReport(Date date, Collection<ItemSalesData> itemSales, double totalRevenue,
                                int totalTransactions, int inStoreTransactions, int onlineTransactions) {
            this.date = date;
            this.itemSales = itemSales;
            this.totalRevenue = totalRevenue;
            this.totalTransactions = totalTransactions;
            this.inStoreTransactions = inStoreTransactions;
            this.onlineTransactions = onlineTransactions;
        }

        // Getters
        public Date getDate() { return date; }
        public Collection<ItemSalesData> getItemSales() { return itemSales; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getTotalTransactions() { return totalTransactions; }
        public int getInStoreTransactions() { return inStoreTransactions; }
        public int getOnlineTransactions() { return onlineTransactions; }
    }

    public static class ItemSalesData {
        private final String itemCode;
        private final String itemName;
        private int inStoreQuantity = 0;
        private int onlineQuantity = 0;
        private double totalRevenue = 0.0;

        public ItemSalesData(String itemCode, String itemName) {
            this.itemCode = itemCode;
            this.itemName = itemName;
        }

        public void addSale(int quantity, double revenue, TransactionType type) {
            if (type == TransactionType.IN_STORE) {
                inStoreQuantity += quantity;
            } else {
                onlineQuantity += quantity;
            }
            totalRevenue += revenue;
        }

        // Getters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public int getInStoreQuantity() { return inStoreQuantity; }
        public int getOnlineQuantity() { return onlineQuantity; }
        public int getTotalQuantity() { return inStoreQuantity + onlineQuantity; }
        public double getTotalRevenue() { return totalRevenue; }
    }

    public static class ReshelvingData {
        private final String itemCode;
        private final String itemName;
        private final int shelfQuantity;
        private final int warehouseQuantity;
        private final int batchCount;

        public ReshelvingData(String itemCode, String itemName, int shelfQuantity,
                              int warehouseQuantity, int batchCount) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.shelfQuantity = shelfQuantity;
            this.warehouseQuantity = warehouseQuantity;
            this.batchCount = batchCount;
        }

        // Getters
        public String getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public int getShelfQuantity() { return shelfQuantity; }
        public int getWarehouseQuantity() { return warehouseQuantity; }
        public int getBatchCount() { return batchCount; }
    }
}