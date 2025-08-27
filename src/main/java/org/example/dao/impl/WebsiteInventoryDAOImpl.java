// File: src/main/java/org/example/dao/impl/WebsiteInventoryDAOImpl.java
package org.example.dao.impl;

import org.example.dao.WebsiteInventoryDAO;
import org.example.model.WebsiteInventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class WebsiteInventoryDAOImpl implements WebsiteInventoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(WebsiteInventoryDAOImpl.class);
    private Connection conn;

    public WebsiteInventoryDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Optional<WebsiteInventory> getWebsiteInventory(String itemCode) {
        String sql = "SELECT * FROM website_inventory WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new WebsiteInventory(rs.getString("item_code"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            logger.error("Error fetching website inventory for item: " + itemCode, e);
        }
        return Optional.empty();
    }

    @Override
    public void updateWebsiteInventory(String itemCode, int quantity) {
        String sql = "UPDATE website_inventory SET quantity = ? WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, itemCode);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Website inventory updated for item {}: quantity = {}", itemCode, quantity);
            }
        } catch (SQLException e) {
            logger.error("Error updating website inventory for item: " + itemCode, e);
            throw new RuntimeException("Failed to update website inventory", e);
        }
    }

    @Override
    public void addWebsiteInventory(WebsiteInventory inventory) {
        String sql = "INSERT INTO website_inventory (item_code, quantity) VALUES (?, ?) ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inventory.getItemCode());
            ps.setInt(2, inventory.getQuantity());
            ps.executeUpdate();
            logger.info("Website inventory added/updated for item: {}", inventory.getItemCode());
        } catch (SQLException e) {
            logger.error("Error adding website inventory for item: " + inventory.getItemCode(), e);
            throw new RuntimeException("Failed to add website inventory", e);
        }
    }

    @Override
    public List<WebsiteInventory> getAllWebsiteInventory() {
        List<WebsiteInventory> inventoryList = new ArrayList<>();
        String sql = "SELECT wi.*, i.name FROM website_inventory wi INNER JOIN items i ON wi.item_code = i.item_code ORDER BY i.name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                inventoryList.add(new WebsiteInventory(rs.getString("item_code"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all website inventory", e);
        }
        return inventoryList;
    }

    @Override
    public List<WebsiteInventory> getAvailableItems() {
        List<WebsiteInventory> availableItems = new ArrayList<>();
        String sql = "SELECT wi.*, i.name FROM website_inventory wi INNER JOIN items i ON wi.item_code = i.item_code WHERE wi.quantity > 0 AND i.is_active = TRUE ORDER BY i.name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                availableItems.add(new WebsiteInventory(rs.getString("item_code"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            logger.error("Error fetching available website items", e);
        }
        return availableItems;
    }

    @Override
    public boolean reduceWebsiteInventory(String itemCode, int quantity) {
        String sql = "UPDATE website_inventory SET quantity = quantity - ? WHERE item_code = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, itemCode);
            ps.setInt(3, quantity);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Website inventory reduced for item {}: quantity reduced by {}", itemCode, quantity);
                return true;
            } else {
                logger.warn("Insufficient website inventory for item: {}. Requested: {}", itemCode, quantity);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error reducing website inventory for item: " + itemCode, e);
            throw new RuntimeException("Failed to reduce website inventory", e);
        }
    }
}
