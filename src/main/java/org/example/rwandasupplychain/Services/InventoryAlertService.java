package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.InventoryAlertDtos.ExpiringBatchAlert;
import org.example.rwandasupplychain.DTOs.InventoryAlertDtos.LowStockAlert;
import org.example.rwandasupplychain.Entities.Batch;
import org.example.rwandasupplychain.Entities.BatchStatus;
import org.example.rwandasupplychain.Entities.SKU;
import org.example.rwandasupplychain.Repositories.BatchRepository;
import org.example.rwandasupplychain.Repositories.SKURepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class InventoryAlertService {

    private final SKURepository skuRepository;
    private final BatchRepository batchRepository;

    public InventoryAlertService(SKURepository skuRepository, BatchRepository batchRepository) {
        this.skuRepository = skuRepository;
        this.batchRepository = batchRepository;
    }

    public List<LowStockAlert> getLowStockAlerts() {
        return skuRepository.findAll().stream()
                .map(sku -> {
                    Integer stock = batchRepository.sumActiveQuantityBySkuId(sku.getId());
                    int current = stock == null ? 0 : stock;
                    return current <= sku.getLowStockThreshold() ? toLowStockAlert(sku, current) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public List<ExpiringBatchAlert> getExpiringBatches(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate horizon = today.plusDays(daysAhead);
        return batchRepository.findByExpiryDateBetweenAndStatus(today, horizon, BatchStatus.ACTIVE).stream()
                .map(this::toExpiringAlert)
                .toList();
    }

    public List<ExpiringBatchAlert> getExpiredBatches() {
        return batchRepository.findByExpiryDateBeforeAndStatus(LocalDate.now(), BatchStatus.ACTIVE).stream()
                .map(this::toExpiringAlert)
                .toList();
    }

    private LowStockAlert toLowStockAlert(SKU sku, int current) {
        return new LowStockAlert(sku.getId(), sku.getSkuCode(), sku.getProduct().getName(), current, sku.getLowStockThreshold());
    }

    private ExpiringBatchAlert toExpiringAlert(Batch batch) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), batch.getExpiryDate());
        return new ExpiringBatchAlert(batch.getId(), batch.getBatchNo(), batch.getSku().getSkuCode(), batch.getExpiryDate(), batch.getQuantity(), days);
    }
}