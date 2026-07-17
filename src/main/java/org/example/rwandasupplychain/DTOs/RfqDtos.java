package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.rwandasupplychain.Enums.RfqStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RfqDtos {

    public record RfqRequest(
            @NotNull UUID buyerId,
            @NotBlank String category,
            String description,
            @NotNull @Positive Integer quantity,
            @NotBlank String unit,
            BigDecimal targetPrice,
            String deliveryDistrict,
            Double deliveryLatitude,
            Double deliveryLongitude,
            LocalDateTime biddingDeadline
    ) {}

    public record RfqResponse(
            UUID id,
            UUID buyerId,
            String category,
            String description,
            Integer quantity,
            String unit,
            BigDecimal targetPrice,
            String deliveryDistrict,
            Double deliveryLatitude,
            Double deliveryLongitude,
            LocalDateTime biddingDeadline,
            RfqStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
