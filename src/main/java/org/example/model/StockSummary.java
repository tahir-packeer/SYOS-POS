package org.example.model;

import java.util.List;

public class StockSummary {
    private String itemCode;
    private int shelfQuantity;
    private int websiteQuantity;
    private int warehouseQuantity;
    private int totalQuantity;
    private List<StockBatch> batches;

    public StockSummary(String itemCode, int shelfQuantity, int websiteQuantity, int warehouseQuantity, int totalQuantity, List<StockBatch> batches) {
        this.itemCode = itemCode;
        this.shelfQuantity = shelfQuantity;
        this.websiteQuantity = websiteQuantity;
        this.warehouseQuantity = warehouseQuantity;
        this.totalQuantity = totalQuantity;
        this.batches = batches;
    }

    public String getItemCode() {
        return itemCode;
    }

    public int getShelfQuantity() {
        return shelfQuantity;
    }

    public int getWebsiteQuantity() {
        return websiteQuantity;
    }

    public int getWarehouseQuantity() {
        return warehouseQuantity;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public List<StockBatch> getBatches() {
        return batches;
    }
}
