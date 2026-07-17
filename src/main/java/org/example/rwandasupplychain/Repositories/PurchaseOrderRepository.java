package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByBuyerId(UUID buyerId);
    List<PurchaseOrder> findBySupplierId(UUID supplierId);
    Optional<PurchaseOrder> findByQuotation_Id(UUID quotationId);
    boolean existsByPoNumber(String poNumber);
}
