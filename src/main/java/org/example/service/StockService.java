// File: src/main/java/org/example/service/StockService.java
package org.example.service;

import org.example.dao.*;
import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced stock service with intelligent reshelving logic
 */
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final StockBatchDAO batchDAO;
    private final ShelfStockDAO shelfDAO;
    private final WebsiteInventoryDAO websiteDAO;

    public StockService(StockBatchDAO batchDAO, ShelfStockDAO shelfDAO, WebsiteInventoryDAO websiteDAO) {
        this.batchDAO = batchDAO;
        this.shelfDAO = shelfDAO;
        this.websiteDAO = websiteDAO;
    }

    /**
     * Reshelve items using FIFO with expiry consideration
     * Priority: Items expiring soon get shelved first, then FIFO by received date
     */
    public boolean reshelveItems(String itemCode) {
        try {
            List<StockBatch> availableBatches = batchDAO.getBatchesForReshelving(itemCode);
            if (availableBatches.isEmpty()) {
                logger.info("No batches available for reshelving: {}", itemCode);
                return false;
            }

            // Sort batches by expiry date first, then by received date (FIFO)
            availableBatches.sort((b1, b2) -> {
                int expiryCompare = b1.getExpiryDate().compareTo(b2.getExpiryDate());
                if (expiryCompare != 0) {
                    return expiryCompare;
                }
                return b1.getReceivedDate().compareTo(b2.getReceivedDate());
            });

            // Move the first (earliest expiring) batch to shelf
            StockBatch batchToMove = availableBatches.get(0);

            Optional<ShelfStock> currentStockOpt = shelfDAO.getShelfStock(itemCode);
            int currentShelfQty = currentStockOpt.map(ShelfStock::getQuantity).orElse(0);
            int newShelfQty = currentShelfQty + batchToMove.getQuantity();

            if (currentStockOpt.isPresent()) {
                shelfDAO.updateShelfStock(itemCode, newShelfQty);
            } else {
                shelfDAO.addShelfStock(new ShelfStock(itemCode, batchToMove.getQuantity()));
            }

            // Mark batch as moved and update quantity
            batchDAO.updateBatchQuantity(batchToMove.getBatchId(), 0);
            batchDAO.markBatchAsMovedToShelf(batchToMove.getBatchId());

            logger.info("Reshelved {} units of {} from batch {}",
                    batchToMove.getQuantity(), itemCode, batchToMove.getBatchId());
            return true;

        } catch (Exception e) {
            logger.error("Error reshelving items for: " + itemCode, e);
            return false;
        }
    }

    /**
     * Receive new stock batch
     */
    public boolean receiveNewBatch(StockBatch batch) {
        try {
            batchDAO.addBatch(batch);
            logger.info("New stock batch received: {} - {} units",
                    batch.getItemCode(), batch.getQuantity());
            return true;
        } catch (Exception e) {
            logger.error("Error receiving new batch for: " + batch.getItemCode(), e);
            return false;
        }
    }

    /**
     * Auto-reshelve items that are running low
     */
    public List<String> autoReshelvelow(String itemCode) {
        List<String> reshelvedItems = new ArrayList<>();

        Optional<ShelfStock> stockOpt = shelfDAO.getShelfStock(itemCode);
        if (stockOpt.isPresent()) {
            ShelfStock stock = stockOpt.get();
            // Auto-reshelve if stock is low (less than 20 units)
            if (stock.getQuantity() < 20) {
                if (reshelveItems(itemCode)) {
                    reshelvedItems.add(itemCode);
                }
            }
        }

        return reshelvedItems;
    }

    /**
     * Get batches expiring within specified days
     */
    public List<StockBatch> getBatchesExpiringSoon(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, days);
        Date futureDate = cal.getTime();

        return batchDAO.getBatchesExpiringBefore(futureDate);
    }

    /**
     * Transfer stock between shelf and website inventory
     */
    public boolean transferStock(String itemCode, int quantity, boolean fromShelfToWebsite) {
        try {
            if (fromShelfToWebsite) {
                // Transfer from shelf to website
                if (shelfDAO.reduceShelfStock(itemCode, quantity)) {
                    Optional<WebsiteInventory> webInvOpt = websiteDAO.getWebsiteInventory(itemCode);
                    int currentWebQty = webInvOpt.map(WebsiteInventory::getQuantity).orElse(0);
                    websiteDAO.updateWebsiteInventory(itemCode, currentWebQty + quantity);
                    logger.info("Transferred {} units of {} from shelf to website", quantity, itemCode);
                    return true;
                } else {
                    logger.warn("Insufficient shelf stock for transfer: {}", itemCode);
                    return false;
                }
            } else {
                // Transfer from website to shelf
                if (websiteDAO.reduceWebsiteInventory(itemCode, quantity)) {
                    Optional<ShelfStock> shelfStockOpt = shelfDAO.getShelfStock(itemCode);
                    int currentShelfQty = shelfStockOpt.map(ShelfStock::getQuantity).orElse(0);
                    shelfDAO.updateShelfStock(itemCode, currentShelfQty + quantity);
                    logger.info("Transferred {} units of {} from website to shelf", quantity, itemCode);
                    return true;
                } else {
                    logger.warn("Insufficient website inventory for transfer: {}", itemCode);
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("Error transferring stock for: " + itemCode, e);
            return false;
        }
    }

    /**
     * Get comprehensive stock summary for an item
     */
    public StockSummary getStockSummary(String itemCode) {
        Optional<ShelfStock> shelfStockOpt = shelfDAO.getShelfStock(itemCode);
        Optional<WebsiteInventory> webInvOpt = websiteDAO.getWebsiteInventory(itemCode);
        List<StockBatch> batches = batchDAO.getBatchesByItemCode(itemCode);

        int shelfQuantity = shelfStockOpt.map(ShelfStock::getQuantity).orElse(0);
        int websiteQuantity = webInvOpt.map(WebsiteInventory::getQuantity).orElse(0);
        int warehouseQuantity = batches.stream()
                .filter(b -> !b.isMovedToShelf() && b.getQuantity() > 0)
                .mapToInt(StockBatch::getQuantity)
                .sum();

        return new StockSummary(itemCode, shelfQuantity, websiteQuantity, warehouseQuantity, batches);
    }

    /**
     * Get all stock batches
     */
    public List<StockBatch> getAllBatches() {
        return batchDAO.getAllBatches();
    }

    /**
     * Get all shelf stock
     */
    public List<ShelfStock> getAllShelfStock() {
        return shelfDAO.getAllShelfStock();
    }

    /**
     * Get all website inventory
     */
    public List<WebsiteInventory> getAllWebsiteInventory() {
        return websiteDAO.getAllWebsiteInventory();
    }

    /**
     * Inner class for stock summary
     */
    public static class StockSummary {
        private final String itemCode;
        private final int shelfQuantity;
        private final int websiteQuantity;
        private final int warehouseQuantity;
        private final int totalQuantity;
        private final List<StockBatch> batches;

        public StockSummary(String itemCode, int shelfQuantity, int websiteQuantity,
                            int warehouseQuantity, List<StockBatch> batches) {
            this.itemCode = itemCode;
            this.shelfQuantity = shelfQuantity;
            this.websiteQuantity = websiteQuantity;
            this.warehouseQuantity = warehouseQuantity;
            this.totalQuantity = shelfQuantity + websiteQuantity + warehouseQuantity;
            this.batches = batches;
        }

        // Getters
        public String getItemCode() { return itemCode; }
        public int getShelfQuantity() { return shelfQuantity; }
        public int getWebsiteQuantity() { return websiteQuantity; }
        public int getWarehouseQuantity() { return warehouseQuantity; }
        public int getTotalQuantity() { return totalQuantity; }
        public List<StockBatch> getBatches() { return batches; }

        @Override
        public String toString() {
            return String.format("StockSummary{item='%s', shelf=%d, website=%d, warehouse=%d, total=%d}",
                    itemCode, shelfQuantity, websiteQuantity, warehouseQuantity, totalQuantity);
        }
    }
}