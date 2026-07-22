package org.example.rwandasupplychain.Controllers.FarmerEndpoint;

import org.example.rwandasupplychain.DTOs.FarmerDtos.OrderDtos.OrderResponse;
import org.example.rwandasupplychain.Services.FarmerService.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Farmer Dashboard - Orders.
 *
 * A farmer can only view orders placed on their own products, and mark a
 * pending order as confirmed. There is deliberately NO create, update, or
 * delete endpoint here - a farmer cannot edit or remove an order once a
 * buyer has placed it.
 *
 * Base path: /api/farmer/{farmerId}/orders
 */
@RestController
@RequestMapping("/api/farmer/{farmerId}/orders")
public class FarmerOrderController {

    private final OrderService orderService;

    public FarmerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // View all orders placed on the farmer's products
    @GetMapping
    public List<OrderResponse> viewMyOrders(@PathVariable UUID farmerId) {
        return orderService.getAllForFarmer(farmerId);
    }

    // View a single order
    @GetMapping("/{orderId}")
    public OrderResponse viewOrder(@PathVariable UUID farmerId, @PathVariable UUID orderId) {
        return orderService.getByIdForFarmer(farmerId, orderId);
    }

    // Mark a pending order as confirmed - the only state change a farmer is allowed to make
    @PatchMapping("/{orderId}/confirm")
    public OrderResponse confirmOrder(@PathVariable UUID farmerId, @PathVariable UUID orderId) {
        return orderService.confirmForFarmer(farmerId, orderId);
    }
}
