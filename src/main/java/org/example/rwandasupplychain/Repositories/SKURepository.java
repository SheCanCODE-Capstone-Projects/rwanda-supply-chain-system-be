package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.SKU;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SKURepository extends JpaRepository<SKU, UUID> {
    Optional<SKU> findBySkuCode(String skuCode);
    Optional<SKU> findByBarcode(String barcode);
    boolean existsBySkuCode(String skuCode);
    List<SKU> findByProduct_Id(UUID productId);
}