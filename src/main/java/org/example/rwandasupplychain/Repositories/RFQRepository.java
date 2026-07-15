package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.RFQ;
import org.example.rwandasupplychain.Entities.RfqStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RFQRepository extends JpaRepository<RFQ, UUID> {
    List<RFQ> findByStatus(RfqStatus status);
    List<RFQ> findByBuyerId(UUID buyerId);
    List<RFQ> findByStatusAndCategoryIgnoreCase(RfqStatus status, String category);
}
