package org.example.rwandasupplychain.Repositories.InvetoryRepositories;

import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
    List<StockMovement> findByBatch_IdOrderByMovementDateDesc(UUID batchId);
}