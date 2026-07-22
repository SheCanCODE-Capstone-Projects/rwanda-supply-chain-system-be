package org.example.rwandasupplychain.Repositories.FarmerRepositories;

import org.example.rwandasupplychain.Entities.FarmerEntities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    // Orders placed against any product owned by this farmer (Product.orgId).
    List<Order> findByProduct_OrgId(UUID orgId);
}
