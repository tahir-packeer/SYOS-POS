// File: src/main/java/org/example/dao/WebsiteInventoryDAO.java
package org.example.dao;

import org.example.model.WebsiteInventory;
import java.util.List;
import java.util.Optional;

public interface WebsiteInventoryDAO {
    Optional<WebsiteInventory> getWebsiteInventory(String itemCode);
    void updateWebsiteInventory(String itemCode, int quantity);
    void addWebsiteInventory(WebsiteInventory inventory);
    List<WebsiteInventory> getAllWebsiteInventory();
    List<WebsiteInventory> getAvailableItems();
    boolean reduceWebsiteInventory(String itemCode, int quantity);
}