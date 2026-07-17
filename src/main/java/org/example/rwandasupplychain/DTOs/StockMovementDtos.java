package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.rwandasupplychain.Entities.MovementType;

import java.time.LocalDateTime;
import java.util.UUID;

public class StockMovementDtos {

    public record StockMovementRequest(
            @NotNull UUID batchId,
            @NotNull MovementType type,
            @NotNull @Positive Integer quantity,
            String reason,
            UUID performedBy
    ) {}

    public record StockMovementResponse(
            UUID id,
            UUID batchId,
            String batchNo,
            MovementType type,
            Integer quantity,
            String reason,
            UUID performedBy,
            LocalDateTime movementDate,
            Integer batchQuantityAfter
    ) {}
}