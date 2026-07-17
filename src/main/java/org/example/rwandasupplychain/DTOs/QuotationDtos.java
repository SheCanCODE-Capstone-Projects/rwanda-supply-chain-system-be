package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.rwandasupplychain.Enums.QuotationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class QuotationDtos {

    public record QuotationRequest(
            @NotNull UUID rfqId,
            @NotNull UUID supplierId,
            @NotNull @Positive BigDecimal unitPrice,
            @NotNull @Positive Integer quantity,
            String message,
            LocalDateTime validUntil
    ) {}

    public record QuotationResponse(
            UUID id,
            UUID rfqId,
            UUID supplierId,
            BigDecimal unitPrice,
            Integer quantity,
            String message,
            QuotationStatus status,
            LocalDateTime validUntil,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
