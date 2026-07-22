package org.example.rwandasupplychain.Repositories.InvetoryRepositories;

import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.Batch;
import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, UUID> {

    List<Batch> findBySku_Id(UUID skuId);

    Optional<Batch> findBySku_IdAndBatchNo(UUID skuId, String batchNo);

    List<Batch> findByExpiryDateBetweenAndStatus(LocalDate start, LocalDate end, BatchStatus status);

    List<Batch> findByExpiryDateBeforeAndStatus(LocalDate date, BatchStatus status);

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM Batch b WHERE b.sku.id = :skuId AND b.status = 'ACTIVE'")
    Integer sumActiveQuantityBySkuId(@Param("skuId") UUID skuId);

    @Query("SELECT b FROM Batch b WHERE b.sku.id = :skuId AND b.status = 'ACTIVE' AND b.quantity > 0 " +
            "ORDER BY b.manufacturingDate ASC, b.createdAt ASC")
    List<Batch> findFifoLayersBySkuId(@Param("skuId") UUID skuId);
}