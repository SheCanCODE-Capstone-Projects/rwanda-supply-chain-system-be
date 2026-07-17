package org.example.rwandasupplychain.DTOs;

import jakarta.validation.constraints.NotNull;
import org.example.rwandasupplychain.Enums.NegotiationRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class NegotiationDtos {

    public record NegotiationRequest(
            @NotNull UUID quotationId,
            @NotNull UUID senderId,
            @NotNull NegotiationRole senderRole,
            BigDecimal proposedPrice,
            Integer proposedQuantity,
            String message
    ) {}

    public record NegotiationResponse(
            UUID id,
            UUID quotationId,
            UUID senderId,
            NegotiationRole senderRole,
            BigDecimal proposedPrice,
            Integer proposedQuantity,
            String message,
            LocalDateTime createdAt
    ) {}
}
