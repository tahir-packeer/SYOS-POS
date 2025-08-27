// File: src/main/java/org/example/dao/StockBatchDAO.java
package org.example.dao;

import org.example.model.StockBatch;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface StockBatchDAO {
    void addBatch(StockBatch batch);
    Optional<StockBatch> getBatchById(int batchId);
    List<StockBatch> getBatchesByItemCode(String itemCode);
    List<StockBatch> getAvailableBatches(String itemCode);
    List<StockBatch> getBatchesExpiringBefore(Date date);
    List<StockBatch> getAllBatches();
    boolean updateBatchQuantity(int batchId, int newQuantity);
    boolean markBatchAsMovedToShelf(int batchId);
    List<StockBatch> getBatchesForReshelving(String itemCode);
}