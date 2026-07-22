package org.example.rwandasupplychain.Services.FarmerService;

import org.example.rwandasupplychain.DTOs.FarmerDtos.WarehouseBookingDtos.WarehouseBookingCreate;
import org.example.rwandasupplychain.DTOs.FarmerDtos.WarehouseBookingDtos.WarehouseBookingResponse;
import org.example.rwandasupplychain.DTOs.FarmerDtos.WarehouseBookingDtos.WarehouseBookingUpdate;
import org.example.rwandasupplychain.Entities.FarmerEntities.BookingStatus;
import org.example.rwandasupplychain.Entities.FarmerEntities.Product;
import org.example.rwandasupplychain.Entities.FarmerEntities.Warehouse;
import org.example.rwandasupplychain.Entities.FarmerEntities.WarehouseBooking;
import org.example.rwandasupplychain.Exceptions.ForbiddenOperationException;
import org.example.rwandasupplychain.Exceptions.InvalidStateException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.FarmerRepositories.ProductRepository;
import org.example.rwandasupplychain.Repositories.FarmerRepositories.WarehouseBookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WarehouseBookingService {

    private final WarehouseBookingRepository warehouseBookingRepository;
    private final WarehouseService warehouseService;
    private final ProductRepository productRepository;

    public WarehouseBookingService(WarehouseBookingRepository warehouseBookingRepository,
                                   WarehouseService warehouseService,
                                   ProductRepository productRepository) {
        this.warehouseBookingRepository = warehouseBookingRepository;
        this.warehouseService = warehouseService;
        this.productRepository = productRepository;
    }

    public WarehouseBookingResponse createForFarmer(UUID farmerId, WarehouseBookingCreate request) {
        if (!request.endDate().isAfter(request.startDate())) {
            throw new InvalidStateException("End date must be after the start date");
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));
        if (!product.getOrgId().equals(farmerId)) {
            throw new ForbiddenOperationException("You can only book warehouse space for your own products");
        }

        Warehouse warehouse = warehouseService.findEntity(request.warehouseId());
        if (!warehouse.isActive()) {
            throw new InvalidStateException("This warehouse is not currently accepting bookings");
        }
        if (warehouse.getAvailableCapacity() < request.quantity()) {
            throw new InvalidStateException("Not enough available capacity in this warehouse ("
                    + warehouse.getAvailableCapacity() + " remaining)");
        }

        warehouse.setAvailableCapacity(warehouse.getAvailableCapacity() - request.quantity());

        WarehouseBooking booking = new WarehouseBooking();
        booking.setFarmerId(farmerId);
        booking.setWarehouse(warehouse);
        booking.setProduct(product);
        booking.setQuantity(request.quantity());
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setNotes(request.notes());
        booking.setStatus(BookingStatus.PENDING);

        return toResponse(warehouseBookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<WarehouseBookingResponse> getAllForFarmer(UUID farmerId) {
        return warehouseBookingRepository.findByFarmerId(farmerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WarehouseBookingResponse getByIdForFarmer(UUID farmerId, UUID id) {
        return toResponse(findOwnedEntity(farmerId, id));
    }

    public WarehouseBookingResponse updateForFarmer(UUID farmerId, UUID id, WarehouseBookingUpdate request) {
        if (!request.endDate().isAfter(request.startDate())) {
            throw new InvalidStateException("End date must be after the start date");
        }

        WarehouseBooking booking = findOwnedEntity(farmerId, id);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidStateException(
                    "This booking can no longer be edited because it is already " + booking.getStatus());
        }

        Warehouse warehouse = booking.getWarehouse();
        int delta = request.quantity() - booking.getQuantity();
        if (delta > 0 && warehouse.getAvailableCapacity() < delta) {
            throw new InvalidStateException("Not enough available capacity in this warehouse ("
                    + warehouse.getAvailableCapacity() + " remaining)");
        }
        warehouse.setAvailableCapacity(warehouse.getAvailableCapacity() - delta);

        booking.setQuantity(request.quantity());
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setNotes(request.notes());

        return toResponse(warehouseBookingRepository.save(booking));
    }

    public WarehouseBookingResponse cancelForFarmer(UUID farmerId, UUID id) {
        WarehouseBooking booking = findOwnedEntity(farmerId, id);
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidStateException(
                    "This booking cannot be cancelled because it is already " + booking.getStatus());
        }

        Warehouse warehouse = booking.getWarehouse();
        warehouse.setAvailableCapacity(warehouse.getAvailableCapacity() + booking.getQuantity());

        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(warehouseBookingRepository.save(booking));
    }

    private WarehouseBooking findOwnedEntity(UUID farmerId, UUID id) {
        WarehouseBooking booking = warehouseBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse booking not found: " + id));
        if (!booking.getFarmerId().equals(farmerId)) {
            throw new ForbiddenOperationException("You do not have permission to access this booking");
        }
        return booking;
    }

    private WarehouseBookingResponse toResponse(WarehouseBooking b) {
        return new WarehouseBookingResponse(
                b.getId(),
                b.getFarmerId(),
                b.getWarehouse().getId(),
                b.getWarehouse().getName(),
                b.getProduct().getId(),
                b.getProduct().getName(),
                b.getQuantity(),
                b.getStartDate(),
                b.getEndDate(),
                b.getNotes(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}