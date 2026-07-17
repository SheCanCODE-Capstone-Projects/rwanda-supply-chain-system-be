package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.Batch;
import org.example.rwandasupplychain.Entities.BatchStatus;
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
}