package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NegotiationRepository extends JpaRepository<Negotiation, UUID> {
    List<Negotiation> findByQuotation_IdOrderByCreatedAtAsc(UUID quotationId);
}
