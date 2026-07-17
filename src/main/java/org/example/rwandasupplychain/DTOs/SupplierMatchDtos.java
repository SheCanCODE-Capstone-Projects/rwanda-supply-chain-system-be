package org.example.rwandasupplychain.DTOs;

import java.math.BigDecimal;
import java.util.UUID;

public class SupplierMatchDtos {

    public record SupplierMatch(
            UUID supplierId,
            String supplierName,
            UUID productId,
            String productName,
            UUID skuId,
            String skuCode,
            BigDecimal price,
            Double distanceKm,
            double score
    ) {}
}
