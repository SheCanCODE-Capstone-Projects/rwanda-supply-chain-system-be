package org.example.rwandasupplychain.DTOs.FarmerDtos;

import org.example.rwandasupplychain.Entities.FarmerEntities.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderDtos {

    // Farmers only ever view orders (and confirm them) - there is no
    // create/update/delete request DTO here on purpose. Order creation
    // belongs to the buyer side, built elsewhere.
    public record OrderResponse(
            UUID id,
            UUID buyerId,
            UUID productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            String deliveryLocation,
            OrderStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
