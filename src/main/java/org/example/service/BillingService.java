// File: src/main/java/org/example/service/BillingService.java
package org.example.service;

import org.example.dao.*;
import org.example.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Enhanced billing service with comprehensive business logic
 */
public class BillingService {
    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private final BillDAO billDAO;
    private final CustomerDAO customerDAO;
    private final ShelfStockDAO shelfStockDAO;
    private final WebsiteInventoryDAO websiteInventoryDAO;
    private final ItemDAO itemDAO;
    private final BillTemplateService billTemplateService;

    public BillingService(BillDAO billDAO, CustomerDAO customerDAO, ShelfStockDAO shelfStockDAO,
            WebsiteInventoryDAO websiteInventoryDAO, ItemDAO itemDAO) {
        this.billDAO = billDAO;
        this.customerDAO = customerDAO;
        this.shelfStockDAO = shelfStockDAO;
        this.websiteInventoryDAO = websiteInventoryDAO;
        this.itemDAO = itemDAO;
        this.billTemplateService = new BillTemplateService(billDAO, customerDAO);
    }

    /**
     * Process billing transaction
     */
    public int processBilling(String phone, String name, List<BillItem> items,
            double cashReceived, TransactionType transactionType) {
        return processBilling(phone, name, items, cashReceived, transactionType, 0.0);
    }

    /**
     * Process billing transaction with custom discount
     */
    public int processBilling(String phone, String name, List<BillItem> items,
            double cashReceived, TransactionType transactionType, double discount) {
        try {
            // Get or create customer
            Customer customer = getOrCreateCustomer(phone, name);
            if (customer == null) {
                logger.error("Failed to get or create customer: {}", phone);
                return -1;
            }

            // Validate and populate item names
            List<BillItem> validatedItems = validateAndPopulateItems(items);
            if (validatedItems.isEmpty()) {
                logger.error("No valid items found for billing");
                return -1;
            }

            // Check stock availability and reduce inventory
            if (!processInventoryReduction(validatedItems, transactionType)) {
                logger.error("Insufficient stock for transaction");
                return -1;
            }

            // Generate bill
            int serialNumber = SerialNumberGenerator.getInstance().getNextSerial();
            Bill bill = new Bill(0, serialNumber, new Date(), customer.getCustomerId(),
                    validatedItems, cashReceived, transactionType, discount);

            logger.info("Bill created with discount: {}, total amount: {}", discount, bill.getTotalAmount());

            // Save bill
            int billId = billDAO.saveBill(bill);
            if (billId > 0) {
                logger.info("Bill processed successfully: billId={}, serial={}, customer={}",
                        billId, serialNumber, customer.getName());

                // Generate and save bill template
                boolean templateSaved = billTemplateService.generateAndSaveBill(billId);
                if (templateSaved) {
                    logger.info("Bill template saved successfully for bill ID: {}", billId);
                } else {
                    logger.warn("Failed to save bill template for bill ID: {}", billId);
                }

                return billId;
            } else {
                // If bill save failed, restore inventory
                restoreInventory(validatedItems, transactionType);
                logger.error("Failed to save bill, inventory restored");
                return -1;
            }

        } catch (Exception e) {
            logger.error("Error processing billing", e);
            return -1;
        }
    }

    /**
     * Display bill template for a specific bill ID
     */
    public void displayBillTemplate(int billId) {
        Optional<Bill> billOpt = billDAO.getBillById(billId);
        if (billOpt.isPresent()) {
            billTemplateService.displayBillTemplate(billOpt.get());
        } else {
            logger.error("Bill not found with ID: {}", billId);
        }
    }

    /**
     * Get bill file path for a specific bill
     */
    public String getBillFilePath(int billId) {
        return billTemplateService.getBillFilePath(billId);
    }

    /**
     * List all saved bill files
     */
    public void listSavedBills() {
        billTemplateService.listSavedBills();
    }

    /**
     * Get item price by code
     */
    public double getItemPrice(String itemCode) {
        Optional<Item> itemOpt = itemDAO.getItemByCode(itemCode);
        return itemOpt.map(Item::getPrice).orElse(-1.0);
    }

    /**
     * Get item details by code
     */
    public Optional<Item> getItemDetails(String itemCode) {
        return itemDAO.getItemByCode(itemCode);
    }

    /**
     * Check if item is available in specified quantity
     */
    public boolean isItemAvailable(String itemCode, int quantity, TransactionType transactionType) {
        if (transactionType == TransactionType.IN_STORE) {
            return shelfStockDAO.getTotalShelfStock(itemCode) >= quantity;
        } else {
            Optional<WebsiteInventory> inventoryOpt = websiteInventoryDAO.getWebsiteInventory(itemCode);
            return inventoryOpt.map(inv -> inv.isAvailable(quantity)).orElse(false);
        }
    }

    private Customer getOrCreateCustomer(String phone, String name) {
        Optional<Customer> customerOpt = customerDAO.getCustomerByPhone(phone);
        if (customerOpt.isPresent()) {
            return customerOpt.get();
        }

        // Create new customer
        Customer newCustomer = new Customer(0, phone, name);
        int customerId = customerDAO.addCustomer(newCustomer);
        if (customerId > 0) {
            return new Customer(customerId, phone, name);
        }
        return null;
    }

    private List<BillItem> validateAndPopulateItems(List<BillItem> items) {
        List<BillItem> validatedItems = new ArrayList<>();
        for (BillItem item : items) {
            Optional<Item> itemDetailsOpt = itemDAO.getItemByCode(item.getItemCode());
            if (itemDetailsOpt.isPresent()) {
                Item itemDetails = itemDetailsOpt.get();
                if (itemDetails.isActive()) {
                    // Create new BillItem with item name populated
                    BillItem validatedItem = new BillItem(
                            item.getItemCode(),
                            itemDetails.getName(),
                            item.getQuantity(),
                            itemDetails.getPrice());
                    validatedItems.add(validatedItem);
                } else {
                    logger.warn("Item is not active: {}", item.getItemCode());
                }
            } else {
                logger.warn("Item not found: {}", item.getItemCode());
            }
        }
        return validatedItems;
    }

    private boolean processInventoryReduction(List<BillItem> items, TransactionType transactionType) {
        List<BillItem> processedItems = new ArrayList<>();

        for (BillItem item : items) {
            boolean success;
            if (transactionType == TransactionType.IN_STORE) {
                success = shelfStockDAO.reduceShelfStock(item.getItemCode(), item.getQuantity());
            } else {
                success = websiteInventoryDAO.reduceWebsiteInventory(item.getItemCode(), item.getQuantity());
            }

            if (success) {
                processedItems.add(item);
            } else {
                // Rollback previous reductions
                restoreInventory(processedItems, transactionType);
                return false;
            }
        }
        return true;
    }

    private void restoreInventory(List<BillItem> items, TransactionType transactionType) {
        for (BillItem item : items) {
            try {
                if (transactionType == TransactionType.IN_STORE) {
                    Optional<ShelfStock> stockOpt = shelfStockDAO.getShelfStock(item.getItemCode());
                    int currentQty = stockOpt.map(ShelfStock::getQuantity).orElse(0);
                    shelfStockDAO.updateShelfStock(item.getItemCode(), currentQty + item.getQuantity());
                } else {
                    Optional<WebsiteInventory> invOpt = websiteInventoryDAO.getWebsiteInventory(item.getItemCode());
                    int currentQty = invOpt.map(WebsiteInventory::getQuantity).orElse(0);
                    websiteInventoryDAO.updateWebsiteInventory(item.getItemCode(), currentQty + item.getQuantity());
                }
            } catch (Exception e) {
                logger.error("Error restoring inventory for item: " + item.getItemCode(), e);
            }
        }
    }
}