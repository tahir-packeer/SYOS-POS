// File: src/main/java/org/example/dao/impl/StockBatchDAOImpl.java
package org.example.dao.impl;

import org.example.dao.StockBatchDAO;
import org.example.model.StockBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class StockBatchDAOImpl implements StockBatchDAO {
    private static final Logger logger = LoggerFactory.getLogger(StockBatchDAOImpl.class);
    private Connection conn;

    public StockBatchDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void addBatch(StockBatch batch) {
        String sql = "INSERT INTO stock_batches (item_code, quantity, received_date, expiry_date, supplier_name, purchase_price, is_moved_to_shelf) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, batch.getItemCode());
            ps.setInt(2, batch.getQuantity());
            ps.setDate(3, new java.sql.Date(batch.getReceivedDate().getTime()));
            ps.setDate(4, new java.sql.Date(batch.getExpiryDate().getTime()));
            ps.setString(5, batch.getSupplierName());
            ps.setDouble(6, batch.getPurchasePrice());
            ps.setBoolean(7, batch.isMovedToShelf());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Stock batch added for item: {}", batch.getItemCode());
            }
        } catch (SQLException e) {
            logger.error("Error adding stock batch for item: " + batch.getItemCode(), e);
            throw new RuntimeException("Failed to add stock batch", e);
        }
    }

    @Override
    public Optional<StockBatch> getBatchById(int batchId) {
        String sql = "SELECT * FROM stock_batches WHERE batch_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(createStockBatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching batch by ID: " + batchId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<StockBatch> getBatchesByItemCode(String itemCode) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE item_code = ? ORDER BY expiry_date ASC, received_date ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                batches.add(createStockBatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching batches for item: " + itemCode, e);
        }
        return batches;
    }

    @Override
    public List<StockBatch> getAvailableBatches(String itemCode) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE item_code = ? AND quantity > 0 ORDER BY expiry_date ASC, received_date ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                batches.add(createStockBatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching available batches for item: " + itemCode, e);
        }
        return batches;
    }

    @Override
    public List<StockBatch> getBatchesExpiringBefore(Date date) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches WHERE expiry_date <= ? AND quantity > 0 ORDER BY expiry_date ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                batches.add(createStockBatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching expiring batches", e);
        }
        return batches;
    }

    @Override
    public List<StockBatch> getAllBatches() {
        List<StockBatch> batches = new ArrayList<>();
        String sql = "SELECT * FROM stock_batches ORDER BY item_code, expiry_date ASC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                batches.add(createStockBatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all batches", e);
        }
        return batches;
    }

    @Override
    public boolean updateBatchQuantity(int batchId, int newQuantity) {
        String sql = "UPDATE stock_batches SET quantity = ? WHERE batch_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, batchId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Batch quantity updated: batchId={}, newQuantity={}", batchId, newQuantity);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating batch quantity: batchId=" + batchId, e);
        }
        return false;
    }

    @Override
    public boolean markBatchAsMovedToShelf(int batchId) {
        String sql = "UPDATE stock_batches SET is_moved_to_shelf = TRUE WHERE batch_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Batch marked as moved to shelf: batchId={}", batchId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error marking batch as moved to shelf: batchId=" + batchId, e);
        }
        return false;
    }

    @Override
    public List<StockBatch> getBatchesForReshelving(String itemCode) {
        List<StockBatch> batches = new ArrayList<>();
        String sql = """
            SELECT * FROM stock_batches 
            WHERE item_code = ? AND quantity > 0 AND is_moved_to_shelf = FALSE
            ORDER BY expiry_date ASC, received_date ASC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemCode);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                batches.add(createStockBatchFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching batches for reshelving: " + itemCode, e);
        }
        return batches;
    }

    private StockBatch createStockBatchFromResultSet(ResultSet rs) throws SQLException {
        StockBatch batch = new StockBatch(
                rs.getInt("batch_id"),
                rs.getString("item_code"),
                rs.getInt("quantity"),
                rs.getDate("received_date"),
                rs.getDate("expiry_date"),
                rs.getString("supplier_name"),
                rs.getDouble("purchase_price")
        );
        batch.setMovedToShelf(rs.getBoolean("is_moved_to_shelf"));
        return batch;
    }
}