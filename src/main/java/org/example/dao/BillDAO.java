// File: src/main/java/org/example/dao/BillDAO.java
package org.example.dao;

import org.example.model.Bill;
import org.example.model.TransactionType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BillDAO {
    int saveBill(Bill bill);
    Optional<Bill> getBillById(int billId);
    Optional<Bill> getBillBySerialNumber(int serialNumber);
    List<Bill> getBillsByDate(Date date);
    List<Bill> getBillsByDateRange(Date startDate, Date endDate);
    List<Bill> getBillsByCustomer(int customerId);
    List<Bill> getBillsByTransactionType(TransactionType transactionType);
    List<Bill> getAllBills();
    int getNextSerialNumber();
}