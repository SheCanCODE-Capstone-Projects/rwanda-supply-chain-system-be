package org.example.rwandasupplychain.Controllers.FarmerEndpoint;

import jakarta.validation.Valid;
import org.example.rwandasupplychain.DTOs.FarmerDtos.TransportRequestDtos.TransportRequestCreate;
import org.example.rwandasupplychain.DTOs.FarmerDtos.TransportRequestDtos.TransportRequestResponse;
import org.example.rwandasupplychain.DTOs.FarmerDtos.TransportRequestDtos.TransportRequestUpdate;
import org.example.rwandasupplychain.Services.FarmerService.TransportRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/farmer/{farmerId}/transport-requests")
public class FarmerTransportController {

    private final TransportRequestService transportRequestService;

    public FarmerTransportController(TransportRequestService transportRequestService) {
        this.transportRequestService = transportRequestService;
    }

    @PostMapping
    public ResponseEntity<TransportRequestResponse> requestTransport(@PathVariable UUID farmerId,
                                                                     @Valid @RequestBody TransportRequestCreate request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transportRequestService.createForFarmer(farmerId, request));
    }
    @GetMapping
    public List<TransportRequestResponse> viewMyTransportRequests(@PathVariable UUID farmerId) {
        return transportRequestService.getAllForFarmer(farmerId);
    }

    @GetMapping("/{transportId}")
    public TransportRequestResponse viewTransportRequest(@PathVariable UUID farmerId, @PathVariable UUID transportId) {
        return transportRequestService.getByIdForFarmer(farmerId, transportId);
    }

    @PutMapping("/{transportId}")
    public TransportRequestResponse updateTransportRequest(@PathVariable UUID farmerId,
                                                           @PathVariable UUID transportId,
                                                           @Valid @RequestBody TransportRequestUpdate request) {
        return transportRequestService.updateForFarmer(farmerId, transportId, request);
    }


    @PatchMapping("/{transportId}/cancel")
    public TransportRequestResponse cancelTransportRequest(@PathVariable UUID farmerId, @PathVariable UUID transportId) {
        return transportRequestService.cancelForFarmer(farmerId, transportId);
    }
}