// File: src/main/java/org/example/dao/impl/BillDAOImpl.java
package org.example.dao.impl;

import org.example.dao.BillDAO;
import org.example.model.Bill;
import org.example.model.BillItem;
import org.example.model.SerialNumberGenerator;
import org.example.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class BillDAOImpl implements BillDAO {
    private static final Logger logger = LoggerFactory.getLogger(BillDAOImpl.class);
    private Connection conn;

    public BillDAOImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public int saveBill(Bill bill) {
        String billSql = """
                INSERT INTO bills (bill_serial_number, bill_date, customer_id, total_amount, discount,
                                 cash_received, change_amount, transaction_type, bill_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String itemSql = """
                INSERT INTO bill_items (bill_id, item_code, item_name, quantity, unit_price, total_price)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        int billId = -1;

        try {
            conn.setAutoCommit(false);

            // Insert bill
            try (PreparedStatement billStmt = conn.prepareStatement(billSql, Statement.RETURN_GENERATED_KEYS)) {
                billStmt.setInt(1, bill.getSerialNumber());
                billStmt.setDate(2, new java.sql.Date(bill.getBillDate().getTime()));
                billStmt.setInt(3, bill.getCustomerId());
                billStmt.setDouble(4, bill.getTotalAmount());
                billStmt.setDouble(5, bill.getDiscount());
                billStmt.setDouble(6, bill.getCashReceived());
                billStmt.setDouble(7, bill.getChangeAmount());
                billStmt.setString(8, bill.getTransactionTypeString());
                billStmt.setString(9, bill.getStatus().toString());

                int rowsAffected = billStmt.executeUpdate();
                if (rowsAffected > 0) {
                    ResultSet keys = billStmt.getGeneratedKeys();
                    if (keys.next()) {
                        billId = keys.getInt(1);
                    }
                }
            }

            // Insert bill items
            if (billId > 0) {
                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                    for (BillItem item : bill.getItems()) {
                        itemStmt.setInt(1, billId);
                        itemStmt.setString(2, item.getItemCode());
                        itemStmt.setString(3, item.getItemName());
                        itemStmt.setInt(4, item.getQuantity());
                        itemStmt.setDouble(5, item.getUnitPrice());
                        itemStmt.setDouble(6, item.getTotalPrice());
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }
            }

            conn.commit();
            logger.info("Bill saved successfully with ID: {}", billId);

        } catch (SQLException e) {
            try {
                conn.rollback();
                logger.error("Bill transaction rolled back", e);
            } catch (SQLException ex) {
                logger.error("Error during rollback", ex);
            }
            throw new RuntimeException("Failed to save bill", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Error resetting auto-commit", e);
            }
        }

        return billId;
    }

    @Override
    public Optional<Bill> getBillById(int billId) {
        String sql = """
                SELECT b.*, c.name as customer_name FROM bills b
                INNER JOIN customers c ON b.customer_id = c.customer_id
                WHERE b.bill_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                List<BillItem> items = getBillItems(billId);
                return Optional.of(createBillFromResultSet(rs, items));
            }
        } catch (SQLException e) {
            logger.error("Error fetching bill by ID: " + billId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Bill> getBillBySerialNumber(int serialNumber) {
        String sql = """
                SELECT b.*, c.name as customer_name FROM bills b
                INNER JOIN customers c ON b.customer_id = c.customer_id
                WHERE b.bill_serial_number = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serialNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int billId = rs.getInt("bill_id");
                List<BillItem> items = getBillItems(billId);
                return Optional.of(createBillFromResultSet(rs, items));
            }
        } catch (SQLException e) {
            logger.error("Error fetching bill by serial number: " + serialNumber, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Bill> getBillsByDate(Date date) {
        List<Bill> bills = new ArrayList<>();
        String sql = """
                SELECT b.*, c.name as customer_name FROM bills b
                INNER JOIN customers c ON b.customer_id = c.customer_id
                WHERE DATE(b.bill_date) = ?
                ORDER BY b.bill_id DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(date.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                List<BillItem> items = getBillItems(billId);
                bills.add(createBillFromResultSet(rs, items));
            }
        } catch (SQLException e) {
            logger.error("Error fetching bills by date: " + date, e);
        }
        return bills;
    }

    @Override
    public List<Bill> getBillsByDateRange(Date startDate, Date endDate) {
        List<Bill> bills = new ArrayList<>();
        String sql = """
                SELECT b.*, c.name as customer_name FROM bills b
                INNER JOIN customers c ON b.customer_id = c.customer_id
                WHERE DATE(b.bill_date) BETWEEN ? AND ?
                ORDER BY b.bill_date DESC, b.bill_id DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, new java.sql.Date(startDate.getTime()));
            ps.setDate(2, new java.sql.Date(endDate.getTime()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                List<BillItem> items = getBillItems(billId);
                bills.add(createBillFromResultSet(rs, items));
            }
        } catch (SQLException e) {
            logger.error("Error fetching bills by date range", e);
        }
        return bills;
    }

    @Override
    public List<Bill> getBillsByCustomer(int customerId) {
        List<Bill> bills = new ArrayList<>();
        String sql = """
                SELECT b.*, c.name as customer_name FROM bills b
                INNER JOIN customers c ON b.customer_id = c.customer_id
                WHERE b.customer_id = ?
                ORDER BY b.bill_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                List<BillItem> items = getBillItems(billId);
                bills.add(createBillFromResultSet(rs, items));
            }
        } catch (SQLException e) {
            logger.error("Error fetching bills by customer: " + customerId, e);
        }
        return bills;
    }

    @Override
    public List<Bill> getBillsByTransactionType(TransactionType transactionType) {
        List<Bill> bills = new ArrayList<>();
        String sql = """
                SELECT b.*, c.name as customer_name FROM bills b
                INNER JOIN customers c ON b.customer_id = c.customer_id
                WHERE b.transaction_type = ?
                ORDER BY b.bill_date DESC
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionType.getValue());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                List<BillItem> items = getBillItems(billId);
                bills.add(createBillFromResultSet(rs, items));
            }
        } catch (SQLException e) {
            logger.error("Error fetching bills by transaction type: " + transactionType, e);
        }
        return bills;
    }

    @Override
    public List<Bill> getAllBills() {
        List<Bill> bills = new ArrayList<>();
        String sql = """
                SELECT b.*, c.name as customer_name FROM bills b
                INNER JOIN customers c ON b.customer_id = c.customer_id
                ORDER BY b.bill_date DESC, b.bill_id DESC
                LIMIT 1000
                """;
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int billId = rs.getInt("bill_id");
                List<BillItem> items = getBillItems(billId);
                bills.add(createBillFromResultSet(rs, items));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all bills", e);
        }
        return bills;
    }

    @Override
    public int getNextSerialNumber() {
        return SerialNumberGenerator.getInstance().getNextSerial();
    }

    private List<BillItem> getBillItems(int billId) {
        List<BillItem> items = new ArrayList<>();
        String sql = "SELECT * FROM bill_items WHERE bill_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BillItem item = new BillItem(
                        rs.getString("item_code"),
                        rs.getString("item_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"));
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Error fetching bill items for bill: " + billId, e);
        }
        return items;
    }

    private Bill createBillFromResultSet(ResultSet rs, List<BillItem> items) throws SQLException {
        TransactionType transactionType = TransactionType.fromString(rs.getString("transaction_type"));

        Bill bill = new Bill(
                rs.getInt("bill_id"),
                rs.getInt("bill_serial_number"),
                rs.getDate("bill_date"),
                rs.getInt("customer_id"),
                items,
                rs.getDouble("cash_received"),
                transactionType,
                rs.getDouble("discount") // Load discount from database
        );

        // Set status if needed
        String status = rs.getString("bill_status");
        if (status != null) {
            bill.setStatus(Bill.BillStatus.valueOf(status));
        }

        return bill;
    }
}