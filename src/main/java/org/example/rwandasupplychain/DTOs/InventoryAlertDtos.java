package org.example.rwandasupplychain.DTOs;

import java.time.LocalDate;
import java.util.UUID;

public class InventoryAlertDtos {

    public record LowStockAlert(
            UUID skuId,
            String skuCode,
            String productName,
            Integer currentStock,
            Integer lowStockThreshold
    ) {}

    public record ExpiringBatchAlert(
            UUID batchId,
            String batchNo,
            String skuCode,
            LocalDate expiryDate,
            Integer quantity,
            long daysUntilExpiry
    ) {}
}