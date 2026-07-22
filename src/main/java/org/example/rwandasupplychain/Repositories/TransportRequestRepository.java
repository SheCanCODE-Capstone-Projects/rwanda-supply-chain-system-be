package org.example.rwandasupplychain.Repositories;

import org.example.rwandasupplychain.Entities.TransportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransportRequestRepository extends JpaRepository<TransportRequest, UUID> {
    List<TransportRequest> findByFarmerId(UUID farmerId);
}