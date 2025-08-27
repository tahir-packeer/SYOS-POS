// File: src/main/java/org/example/dao/ShelfStockDAO.java
package org.example.dao;

import org.example.model.ShelfStock;
import java.util.List;
import java.util.Optional;

public interface ShelfStockDAO {
    Optional<ShelfStock> getShelfStock(String itemCode);
    void updateShelfStock(String itemCode, int quantity);
    void addShelfStock(ShelfStock stock);
    List<ShelfStock> getAllShelfStock();
    List<ShelfStock> getLowStockItems(int threshold);
    boolean reduceShelfStock(String itemCode, int quantity);
    int getTotalShelfStock(String itemCode);
}