
// File: src/main/java/org/example/dao/impl/ShelfStockDAOImpl.java
package org.example.dao.impl;

import org.example.dao.ShelfStockDAO;
import org.example.model.ShelfStock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class ShelfStockDAOImpl implements ShelfStockDAO {
    private static final Logger logger = LoggerFactory.getLogger(ShelfStockDAOImpl.class);
    private Connection conn;

    public ShelfStockDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Optional<ShelfStock> getShelfStock(String itemCode) {
        String sql = "SELECT * FROM shelf_stock WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new ShelfStock(rs.getString("item_code"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            logger.error("Error fetching shelf stock for item: " + itemCode, e);
        }
        return Optional.empty();
    }

    @Override
    public void updateShelfStock(String itemCode, int quantity) {
        String sql = "UPDATE shelf_stock SET quantity = ? WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, itemCode);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Shelf stock updated for item {}: quantity = {}", itemCode, quantity);
            }
        } catch (SQLException e) {
            logger.error("Error updating shelf stock for item: " + itemCode, e);
            throw new RuntimeException("Failed to update shelf stock", e);
        }
    }

    @Override
    public void addShelfStock(ShelfStock stock) {
        String sql = "INSERT INTO shelf_stock (item_code, quantity) VALUES (?, ?) ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stock.getItemCode());
            ps.setInt(2, stock.getQuantity());
            ps.executeUpdate();
            logger.info("Shelf stock added/updated for item: {}", stock.getItemCode());
        } catch (SQLException e) {
            logger.error("Error adding shelf stock for item: " + stock.getItemCode(), e);
            throw new RuntimeException("Failed to add shelf stock", e);
        }
    }

    @Override
    public List<ShelfStock> getAllShelfStock() {
        List<ShelfStock> stockList = new ArrayList<>();
        String sql = "SELECT ss.*, i.name FROM shelf_stock ss INNER JOIN items i ON ss.item_code = i.item_code ORDER BY i.name";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                stockList.add(new ShelfStock(rs.getString("item_code"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all shelf stock", e);
        }
        return stockList;
    }

    @Override
    public List<ShelfStock> getLowStockItems(int threshold) {
        List<ShelfStock> lowStockItems = new ArrayList<>();
        String sql = "SELECT ss.*, i.name FROM shelf_stock ss INNER JOIN items i ON ss.item_code = i.item_code WHERE ss.quantity < ? ORDER BY ss.quantity ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lowStockItems.add(new ShelfStock(rs.getString("item_code"), rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            logger.error("Error fetching low stock items", e);
        }
        return lowStockItems;
    }

    @Override
    public boolean reduceShelfStock(String itemCode, int quantity) {
        String sql = "UPDATE shelf_stock SET quantity = quantity - ? WHERE item_code = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setString(2, itemCode);
            ps.setInt(3, quantity);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Shelf stock reduced for item {}: quantity reduced by {}", itemCode, quantity);
                return true;
            } else {
                logger.warn("Insufficient shelf stock for item: {}. Requested: {}", itemCode, quantity);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error reducing shelf stock for item: " + itemCode, e);
            throw new RuntimeException("Failed to reduce shelf stock", e);
        }
    }

    @Override
    public int getTotalShelfStock(String itemCode) {
        String sql = "SELECT COALESCE(quantity, 0) FROM shelf_stock WHERE item_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error getting total shelf stock for item: " + itemCode, e);
        }
        return 0;
    }
}