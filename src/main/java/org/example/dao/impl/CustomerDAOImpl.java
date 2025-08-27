// File: src/main/java/org/example/dao/impl/CustomerDAOImpl.java
package org.example.dao.impl;

import org.example.dao.CustomerDAO;
import org.example.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDAOImpl implements CustomerDAO {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAOImpl.class);
    private Connection conn;

    public CustomerDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Optional<Customer> getCustomerByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("phone"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getObject("user_id", Integer.class)
                );
                return Optional.of(customer);
            }
        } catch (SQLException e) {
            logger.error("Error fetching customer by phone: " + phone, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Customer> getCustomerById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("phone"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getObject("user_id", Integer.class)
                );
                return Optional.of(customer);
            }
        } catch (SQLException e) {
            logger.error("Error fetching customer by ID: " + customerId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("phone"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getObject("user_id", Integer.class)
                );
                customers.add(customer);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all customers", e);
        }
        return customers;
    }

    @Override
    public int addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (phone, name, email, address, user_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getPhone());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setObject(5, customer.getUserId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int customerId = rs.getInt(1);
                    logger.info("Customer created successfully: {}", customer.getName());
                    return customerId;
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating customer: " + customer.getName(), e);
        }
        return -1;
    }

    @Override
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET name = ?, email = ?, address = ? WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getAddress());
            ps.setInt(4, customer.getCustomerId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Customer updated successfully: {}", customer.getName());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating customer: " + customer.getCustomerId(), e);
        }
        return false;
    }

    @Override
    public List<Customer> getCustomersByTransactionType(String transactionType) {
        List<Customer> customers = new ArrayList<>();
        String sql = """
            SELECT DISTINCT c.* FROM customers c 
            INNER JOIN bills b ON c.customer_id = b.customer_id 
            WHERE b.transaction_type = ?
            ORDER BY c.name
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("phone"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getObject("user_id", Integer.class)
                );
                customers.add(customer);
            }
        } catch (SQLException e) {
            logger.error("Error fetching customers by transaction type: " + transactionType, e);
        }
        return customers;
    }
}