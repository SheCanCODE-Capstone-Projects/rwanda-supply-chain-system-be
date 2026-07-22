package org.example.rwandasupplychain.Controllers.FarmerEndpoint;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.FarmerDtos.WarehouseBookingDtos.WarehouseBookingCreate;
import org.example.rwandasupplychain.DTOs.FarmerDtos.WarehouseBookingDtos.WarehouseBookingResponse;
import org.example.rwandasupplychain.DTOs.FarmerDtos.WarehouseBookingDtos.WarehouseBookingUpdate;
import org.example.rwandasupplychain.Services.FarmerService.WarehouseBookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/farmer/{farmerId}/warehouse-bookings")
public class FarmerWarehouseBookingController {

    private final WarehouseBookingService warehouseBookingService;

    public FarmerWarehouseBookingController(WarehouseBookingService warehouseBookingService) {
        this.warehouseBookingService = warehouseBookingService;
    }

    @PostMapping
    public ResponseEntity<WarehouseBookingResponse> bookWarehouse(@PathVariable UUID farmerId,
                                                                  @Valid @RequestBody WarehouseBookingCreate request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseBookingService.createForFarmer(farmerId, request));
    }

    @GetMapping
    public List<WarehouseBookingResponse> viewMyBookings(@PathVariable UUID farmerId) {
        return warehouseBookingService.getAllForFarmer(farmerId);
    }

    @GetMapping("/{bookingId}")
    public WarehouseBookingResponse viewBooking(@PathVariable UUID farmerId, @PathVariable UUID bookingId) {
        return warehouseBookingService.getByIdForFarmer(farmerId, bookingId);
    }

    @PutMapping("/{bookingId}")
    public WarehouseBookingResponse updateBooking(@PathVariable UUID farmerId,
                                                  @PathVariable UUID bookingId,
                                                  @Valid @RequestBody WarehouseBookingUpdate request) {
        return warehouseBookingService.updateForFarmer(farmerId, bookingId, request);
    }


    @PatchMapping("/{bookingId}/cancel")
    public WarehouseBookingResponse cancelBooking(@PathVariable UUID farmerId, @PathVariable UUID bookingId) {
        return warehouseBookingService.cancelForFarmer(farmerId, bookingId);
    }
}