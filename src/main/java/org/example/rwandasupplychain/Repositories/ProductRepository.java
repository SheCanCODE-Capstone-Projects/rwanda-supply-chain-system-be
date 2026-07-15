package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByOrgId(UUID orgId);
    boolean existsByNameIgnoreCaseAndOrgId(String name, UUID orgId);
    List<Product> findByCategoryIgnoreCaseAndActiveTrue(String category);
}