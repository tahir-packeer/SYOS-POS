// File: src/main/java/org/example/model/WebsiteInventory.java
package org.example.model;

import java.time.LocalDateTime;

public class WebsiteInventory {
    private String itemCode;
    private int quantity;
    private LocalDateTime lastUpdated;

    public WebsiteInventory(String itemCode, int quantity) {
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

    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }

    @Override
    public String toString() {
        return String.format("WebsiteInventory{item='%s', qty=%d}", itemCode, quantity);
    }
}