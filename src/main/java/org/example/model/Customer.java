// File: src/main/java/org/example/model/Customer.java
package org.example.model;

import java.time.LocalDateTime;

public class Customer {
    private int customerId;
    private String phone;
    private String name;
    private String email;
    private String address;
    private Integer userId; // for online customers
    private LocalDateTime createdAt;

    public Customer(int customerId, String phone, String name) {
        this.customerId = customerId;
        this.phone = phone;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public Customer(int customerId, String phone, String name, String email, String address, Integer userId) {
        this.customerId = customerId;
        this.phone = phone;
        this.name = name;
        this.email = email;
        this.address = address;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getCustomerId() { return customerId; }
    public String getPhone() { return phone; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public Integer getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setUserId(Integer userId) { this.userId = userId; }

    @Override
    public String toString() {
        return String.format("Customer{id=%d, name='%s', phone='%s'}", customerId, name, phone);
    }
}