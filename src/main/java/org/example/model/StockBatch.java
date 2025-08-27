// File: src/main/java/org/example/model/StockBatch.java
package org.example.model;

import java.time.LocalDateTime;
import java.util.Date;

public class StockBatch {
    private int batchId;
    private String itemCode;
    private int quantity;
    private Date receivedDate;
    private Date expiryDate;
    private String supplierName;
    private double purchasePrice;
    private boolean isMovedToShelf;
    private LocalDateTime createdAt;

    public StockBatch(int batchId, String itemCode, int quantity, Date receivedDate, Date expiryDate) {
        this.batchId = batchId;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.receivedDate = receivedDate;
        this.expiryDate = expiryDate;
        this.isMovedToShelf = false;
        this.createdAt = LocalDateTime.now();
    }

    public StockBatch(int batchId, String itemCode, int quantity, Date receivedDate,
                      Date expiryDate, String supplierName, double purchasePrice) {
        this.batchId = batchId;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.receivedDate = receivedDate;
        this.expiryDate = expiryDate;
        this.supplierName = supplierName;
        this.purchasePrice = purchasePrice;
        this.isMovedToShelf = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getBatchId() { return batchId; }
    public String getItemCode() { return itemCode; }
    public int getQuantity() { return quantity; }
    public Date getReceivedDate() { return receivedDate; }
    public Date getExpiryDate() { return expiryDate; }
    public String getSupplierName() { return supplierName; }
    public double getPurchasePrice() { return purchasePrice; }
    public boolean isMovedToShelf() { return isMovedToShelf; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public void setMovedToShelf(boolean movedToShelf) { this.isMovedToShelf = movedToShelf; }

    @Override
    public String toString() {
        return String.format("StockBatch{id=%d, item='%s', qty=%d, expiry=%s}",
                batchId, itemCode, quantity, expiryDate);
    }
}