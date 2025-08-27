// File: src/main/java/org/example/dao/impl/ItemDAOImpl.java
package org.example.dao.impl;

import org.example.dao.ItemDAO;
import org.example.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class ItemDAOImpl implements ItemDAO {
    private static final Logger logger = LoggerFactory.getLogger(ItemDAOImpl.class);
    private Connection conn;

    public ItemDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void addItem(Item item) {
        String sql = "INSERT INTO items (item_code, name, price, category, description, min_stock_level, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemCode());
            ps.setString(2, item.getName());
            ps.setDouble(3, item.getPrice());
            ps.setString(4, item.getCategory());
            ps.setString(5, item.getDescription());
            ps.setInt(6, item.getMinStockLevel());
            ps.setBoolean(7, item.isActive());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Item added successfully: {}", item.getItemCode());
            }
        } catch (SQLException e) {
            logger.error("Error adding item: " + item.getItemCode(), e);
            throw new RuntimeException("Failed to add item", e);
        }
    }

    @Override
    public Optional<Item> getItemByCode(String code) {
        String sql = "SELECT * FROM items WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Item item = new Item(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getInt("min_stock_level")
                );
                item.setActive(rs.getBoolean("is_active"));
                return Optional.of(item);
            }
        } catch (SQLException e) {
            logger.error("Error fetching item by code: " + code, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Item item = new Item(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getInt("min_stock_level")
                );
                item.setActive(rs.getBoolean("is_active"));
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Error fetching all items", e);
        }
        return items;
    }

    @Override
    public List<Item> getItemsByCategory(String category) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE category = ? AND is_active = TRUE ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getInt("min_stock_level")
                );
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Error fetching items by category: " + category, e);
        }
        return items;
    }

    @Override
    public List<Item> getActiveItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE is_active = TRUE ORDER BY name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Item item = new Item(
                        rs.getString("item_code"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getInt("min_stock_level")
                );
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Error fetching active items", e);
        }
        return items;
    }

    @Override
    public boolean updateItem(Item item) {
        String sql = "UPDATE items SET name = ?, price = ?, category = ?, description = ?, min_stock_level = ?, is_active = ? WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getDescription());
            ps.setInt(5, item.getMinStockLevel());
            ps.setBoolean(6, item.isActive());
            ps.setString(7, item.getItemCode());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Item updated successfully: {}", item.getItemCode());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating item: " + item.getItemCode(), e);
        }
        return false;
    }

    @Override
    public boolean deactivateItem(String code) {
        String sql = "UPDATE items SET is_active = FALSE WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Item deactivated: {}", code);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deactivating item: " + code, e);
        }
        return false;
    }

    @Override
    public boolean isItemCodeExists(String code) {
        String sql = "SELECT COUNT(*) FROM items WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking item code existence: " + code, e);
        }
        return false;
    }
}
