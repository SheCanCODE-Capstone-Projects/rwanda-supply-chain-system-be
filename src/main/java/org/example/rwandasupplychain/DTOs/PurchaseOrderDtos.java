package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.NotNull;
import org.example.rwandasupplychain.Enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PurchaseOrderDtos {

    public record PurchaseOrderStatusUpdateRequest(
            @NotNull PurchaseOrderStatus status
    ) {}

    public record PurchaseOrderResponse(
            UUID id,
            String poNumber,
            UUID rfqId,
            UUID quotationId,
            UUID buyerId,
            UUID supplierId,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal totalAmount,
            PurchaseOrderStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
