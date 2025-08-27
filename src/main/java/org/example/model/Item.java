// File: src/main/java/org/example/model/Item.java
package org.example.model;

import java.time.LocalDateTime;

public class Item {
    private String itemCode;
    private String name;
    private double price;
    private String category;
    private String description;
    private int minStockLevel;
    private boolean isActive;
    private LocalDateTime createdAt;

    public Item(String itemCode, String name, double price, String category) {
        this.itemCode = itemCode;
        this.name = name;
        this.price = price;
        this.category = category;
        this.minStockLevel = 50;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public Item(String itemCode, String name, double price, String category, String description, int minStockLevel) {
        this.itemCode = itemCode;
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.minStockLevel = minStockLevel;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public String getItemCode() { return itemCode; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public int getMinStockLevel() { return minStockLevel; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return String.format("Item{code='%s', name='%s', price=%.2f, category='%s'}",
                itemCode, name, price, category);
    }
}