// File: src/main/java/org/example/dao/ItemDAO.java
package org.example.dao;

import org.example.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemDAO {
    void addItem(Item item);
    Optional<Item> getItemByCode(String code);
    List<Item> getAllItems();
    List<Item> getItemsByCategory(String category);
    List<Item> getActiveItems();
    boolean updateItem(Item item);
    boolean deactivateItem(String code);
    boolean isItemCodeExists(String code);
}