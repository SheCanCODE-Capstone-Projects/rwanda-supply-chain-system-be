package org.example.rwandasupplychain.DTOs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CertificationDtos {

    public record CertificationResponse(
            UUID id,
            UUID productId,
            String certificationName,
            String issuingBody,
            String certificateNumber,
            LocalDate issueDate,
            LocalDate expiryDate,
            String fileUrl,
            LocalDateTime uploadedAt
    ) {}
}