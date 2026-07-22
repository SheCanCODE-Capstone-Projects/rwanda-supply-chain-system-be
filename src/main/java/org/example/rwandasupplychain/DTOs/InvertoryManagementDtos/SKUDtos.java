package org.example.rwandasupplychain.DTOs.InvertoryManagementDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class SKUDtos {

    public record SKURequest(
            @NotNull UUID productId,
            @NotBlank String skuCode,
            @NotNull @Positive BigDecimal price,
            @NotBlank String unit,
            @PositiveOrZero Integer lowStockThreshold
    ) {}

    public record SKUResponse(
            UUID id,
            UUID productId,
            String productName,
            String skuCode,
            BigDecimal price,
            String unit,
            String barcode,
            Integer lowStockThreshold,
            Integer currentStock,
            boolean active,
            LocalDateTime createdAt
    ) {}
}