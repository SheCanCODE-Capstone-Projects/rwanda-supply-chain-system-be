package org.example.rwandasupplychain.Services.FarmerService;

import org.example.rwandasupplychain.DTOs.FarmerDtos.OrderDtos.OrderResponse;
import org.example.rwandasupplychain.Entities.FarmerEntities.Order;
import org.example.rwandasupplychain.Entities.FarmerEntities.OrderStatus;
import org.example.rwandasupplychain.Exceptions.ForbiddenOperationException;
import org.example.rwandasupplychain.Exceptions.InvalidStateException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.FarmerRepositories.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * A farmer can only VIEW orders placed against their own products, and
 * mark a pending one as CONFIRMED. There is intentionally no create,
 * update, or delete here - order creation is a buyer-side concern, and
 * a farmer cannot edit or remove an order once a buyer has placed it.
 */
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllForFarmer(UUID farmerId) {
        return orderRepository.findByProduct_OrgId(farmerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getByIdForFarmer(UUID farmerId, UUID id) {
        return toResponse(findOwnedEntity(farmerId, id));
    }

    public OrderResponse confirmForFarmer(UUID farmerId, UUID id) {
        Order order = findOwnedEntity(farmerId, id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStateException(
                    "This order can no longer be confirmed because it is already " + order.getStatus());
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return toResponse(orderRepository.save(order));
    }

    private Order findOwnedEntity(UUID farmerId, UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (!order.getProduct().getOrgId().equals(farmerId)) {
            throw new ForbiddenOperationException("You do not have permission to access this order");
        }
        return order;
    }

    private OrderResponse toResponse(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getBuyerId(),
                o.getProduct().getId(),
                o.getProduct().getName(),
                o.getQuantity(),
                o.getUnitPrice(),
                o.getTotalAmount(),
                o.getDeliveryLocation(),
                o.getStatus(),
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
