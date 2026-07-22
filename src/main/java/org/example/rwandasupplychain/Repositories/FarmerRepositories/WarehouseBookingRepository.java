package org.example.rwandasupplychain.Repositories.FarmerRepositories;

import org.example.rwandasupplychain.Entities.FarmerEntities.WarehouseBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WarehouseBookingRepository extends JpaRepository<WarehouseBooking, UUID> {
    List<WarehouseBooking> findByFarmerId(UUID farmerId);
}