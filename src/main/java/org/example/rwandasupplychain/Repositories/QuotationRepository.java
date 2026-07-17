package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.Quotation;
import org.example.rwandasupplychain.Enums.QuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuotationRepository extends JpaRepository<Quotation, UUID> {
    List<Quotation> findByRfq_Id(UUID rfqId);
    List<Quotation> findByRfq_IdAndStatus(UUID rfqId, QuotationStatus status);
    List<Quotation> findBySupplierId(UUID supplierId);
    List<Quotation> findByRfq_IdAndStatusNot(UUID rfqId, QuotationStatus status);
}
