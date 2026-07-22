package org.example.rwandasupplychain.Repositories.FarmerRepositories;

import org.example.rwandasupplychain.Entities.FarmerEntities.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    List<Warehouse> findByActiveTrue();
}