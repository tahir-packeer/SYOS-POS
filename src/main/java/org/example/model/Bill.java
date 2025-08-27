// File: src/main/java/org/example/model/Bill.java
package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class Bill {
    private int billId;
    private int serialNumber;
    private Date billDate;
    private int customerId;
    private double totalAmount;
    private double discount;
    private double cashReceived;
    private double changeAmount;
    private TransactionType transactionType;
    private BillStatus status;
    private List<BillItem> items;
    private LocalDateTime createdAt;

    public enum BillStatus {
        COMPLETED, PENDING, CANCELLED
    }

    public Bill(int billId, int serialNumber, Date billDate, int customerId,
            List<BillItem> items, double cashReceived, TransactionType transactionType) {
        this.billId = billId;
        this.serialNumber = serialNumber;
        this.billDate = billDate;
        this.customerId = customerId;
        this.items = items;
        this.cashReceived = cashReceived;
        this.transactionType = transactionType;
        this.discount = 0.0; // Initialize discount to 0
        this.status = BillStatus.COMPLETED;
        this.createdAt = LocalDateTime.now();

        calculateTotals();
    }

    public Bill(int billId, int serialNumber, Date billDate, int customerId,
            List<BillItem> items, double cashReceived, TransactionType transactionType, double discount) {
        this.billId = billId;
        this.serialNumber = serialNumber;
        this.billDate = billDate;
        this.customerId = customerId;
        this.items = items;
        this.cashReceived = cashReceived;
        this.transactionType = transactionType;
        this.discount = discount;
        this.status = BillStatus.COMPLETED;
        this.createdAt = LocalDateTime.now();

        calculateTotals();
    }

    private void calculateTotals() {
        double subtotal = items.stream()
                .mapToDouble(item -> item.getTotalPrice())
                .sum();

        // Only use manually set discount, no automatic calculation
        this.totalAmount = round(subtotal - discount, 2);
        this.changeAmount = round(cashReceived - totalAmount, 2);
    }

    private double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // Getters
    public int getBillId() {
        return billId;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public Date getBillDate() {
        return billDate;
    }

    public int getCustomerId() {
        return customerId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public double getCashReceived() {
        return cashReceived;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public String getTransactionTypeString() {
        return transactionType.getValue();
    }

    public BillStatus getStatus() {
        return status;
    }

    public List<BillItem> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(BillStatus status) {
        this.status = status;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        calculateTotals(); // Recalculate totals when discount changes
    }

    @Override
    public String toString() {
        return String.format("Bill{id=%d, serial=%d, total=%.2f, type=%s}",
                billId, serialNumber, totalAmount, transactionType);
    }
}