package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.rwandasupplychain.Entities.ValuationMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ValuationDtos {

    public record BatchLayer(
            UUID batchId,
            String batchNo,
            Integer quantity,
            BigDecimal unitCost,
            BigDecimal layerValue,
            LocalDate manufacturingDate,
            LocalDate expiryDate
    ) {}

    public record SkuValuation(
            UUID skuId,
            String skuCode,
            UUID productId,
            String productName,
            ValuationMethod method,
            Integer totalQuantity,
            BigDecimal averageUnitCost,
            BigDecimal totalValue,
            List<BatchLayer> layers
    ) {}

    public record ProductValuation(
            UUID productId,
            String productName,
            ValuationMethod method,
            Integer totalQuantity,
            BigDecimal totalValue,
            List<SkuValuation> skuValuations
    ) {}

    public record InventoryValuationSummary(
            ValuationMethod method,
            LocalDateTime asOf,
            Integer skuCount,
            Integer totalQuantity,
            BigDecimal totalValue,
            List<SkuValuation> skuValuations
    ) {}

    public record FifoCogsRequest(
            @NotNull UUID skuId,
            @NotNull @Positive Integer quantity
    ) {}

    public record ConsumedLayer(
            UUID batchId,
            String batchNo,
            Integer quantityConsumed,
            BigDecimal unitCost,
            BigDecimal cost
    ) {}

    public record FifoCogsResult(
            UUID skuId,
            String skuCode,
            Integer quantityRequested,
            Integer quantityAvailable,
            boolean fullyCovered,
            BigDecimal totalCost,
            BigDecimal averageUnitCost,
            List<ConsumedLayer> consumedLayers
    ) {}
}