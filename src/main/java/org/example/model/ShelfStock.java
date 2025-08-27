// File: src/main/java/org/example/model/ShelfStock.java
package org.example.model;

import java.time.LocalDateTime;

public class ShelfStock {
    private String itemCode;
    private int quantity;
    private LocalDateTime lastUpdated;

    public ShelfStock(String itemCode, int quantity) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters
    public String getItemCode() { return itemCode; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    // Setters
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    public boolean needsReorder(int threshold) {
        return quantity < threshold;
    }

    @Override
    public String toString() {
        return String.format("ShelfStock{item='%s', qty=%d}", itemCode, quantity);
    }
}