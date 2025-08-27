// File: src/main/java/org/example/model/BillItem.java
package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BillItem {
    private String itemCode;
    private String itemName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    public BillItem(String itemCode, String itemName, int quantity, double unitPrice) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = round(quantity * unitPrice, 2);
    }

    public BillItem(String itemCode, int quantity, double unitPrice) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = round(quantity * unitPrice, 2);
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // Getters
    public String getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }

    // For backward compatibility
    public double getPrice() { return unitPrice; }

    // Setters
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        return String.format("BillItem{code='%s', name='%s', qty=%d, price=%.2f, total=%.2f}",
                itemCode, itemName, quantity, unitPrice, totalPrice);
    }
}