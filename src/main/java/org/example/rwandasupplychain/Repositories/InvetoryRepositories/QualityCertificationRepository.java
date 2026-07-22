package org.example.rwandasupplychain.Repositories.InvetoryRepositories;

import org.example.rwandasupplychain.Entities.InvetoryManagementEntities.QualityCertification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QualityCertificationRepository extends JpaRepository<QualityCertification, UUID> {
    List<QualityCertification> findByProduct_Id(UUID productId);
}