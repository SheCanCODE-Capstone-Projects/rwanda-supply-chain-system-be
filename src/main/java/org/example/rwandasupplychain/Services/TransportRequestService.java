package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.TransportRequestDtos.TransportRequestCreate;
import org.example.rwandasupplychain.DTOs.TransportRequestDtos.TransportRequestResponse;
import org.example.rwandasupplychain.DTOs.TransportRequestDtos.TransportRequestUpdate;
import org.example.rwandasupplychain.Entities.Product;
import org.example.rwandasupplychain.Entities.TransportRequest;
import org.example.rwandasupplychain.Entities.TransportStatus;
import org.example.rwandasupplychain.Exceptions.ForbiddenOperationException;
import org.example.rwandasupplychain.Exceptions.InvalidStateException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.ProductRepository;
import org.example.rwandasupplychain.Repositories.TransportRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransportRequestService {

    private final TransportRequestRepository transportRequestRepository;
    private final ProductRepository productRepository;

    public TransportRequestService(TransportRequestRepository transportRequestRepository,
                                   ProductRepository productRepository) {
        this.transportRequestRepository = transportRequestRepository;
        this.productRepository = productRepository;
    }

    public TransportRequestResponse createForFarmer(UUID farmerId, TransportRequestCreate request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));
        if (!product.getOrgId().equals(farmerId)) {
            throw new ForbiddenOperationException("You can only request transport for your own products");
        }

        TransportRequest transportRequest = new TransportRequest();
        transportRequest.setFarmerId(farmerId);
        transportRequest.setProduct(product);
        transportRequest.setQuantity(request.quantity());
        transportRequest.setPickupLocation(request.pickupLocation());
        transportRequest.setDropoffLocation(request.dropoffLocation());
        transportRequest.setPreferredDate(request.preferredDate());
        transportRequest.setNotes(request.notes());
        transportRequest.setStatus(TransportStatus.REQUESTED);

        return toResponse(transportRequestRepository.save(transportRequest));
    }

    @Transactional(readOnly = true)
    public List<TransportRequestResponse> getAllForFarmer(UUID farmerId) {
        return transportRequestRepository.findByFarmerId(farmerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TransportRequestResponse getByIdForFarmer(UUID farmerId, UUID id) {
        return toResponse(findOwnedEntity(farmerId, id));
    }

    public TransportRequestResponse updateForFarmer(UUID farmerId, UUID id, TransportRequestUpdate request) {
        TransportRequest transportRequest = findOwnedEntity(farmerId, id);
        if (transportRequest.getStatus() != TransportStatus.REQUESTED) {
            throw new InvalidStateException(
                    "This transport request can no longer be edited because it is already " + transportRequest.getStatus());
        }
        transportRequest.setQuantity(request.quantity());
        transportRequest.setPickupLocation(request.pickupLocation());
        transportRequest.setDropoffLocation(request.dropoffLocation());
        transportRequest.setPreferredDate(request.preferredDate());
        transportRequest.setNotes(request.notes());
        return toResponse(transportRequestRepository.save(transportRequest));
    }

    public TransportRequestResponse cancelForFarmer(UUID farmerId, UUID id) {
        TransportRequest transportRequest = findOwnedEntity(farmerId, id);
        if (transportRequest.getStatus() == TransportStatus.DELIVERED
                || transportRequest.getStatus() == TransportStatus.CANCELLED) {
            throw new InvalidStateException(
                    "This transport request cannot be cancelled because it is already " + transportRequest.getStatus());
        }
        transportRequest.setStatus(TransportStatus.CANCELLED);
        return toResponse(transportRequestRepository.save(transportRequest));
    }

    private TransportRequest findOwnedEntity(UUID farmerId, UUID id) {
        TransportRequest transportRequest = transportRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transport request not found: " + id));
        if (!transportRequest.getFarmerId().equals(farmerId)) {
            throw new ForbiddenOperationException("You do not have permission to access this transport request");
        }
        return transportRequest;
    }

    private TransportRequestResponse toResponse(TransportRequest t) {
        return new TransportRequestResponse(
                t.getId(),
                t.getFarmerId(),
                t.getProduct().getId(),
                t.getProduct().getName(),
                t.getQuantity(),
                t.getPickupLocation(),
                t.getDropoffLocation(),
                t.getPreferredDate(),
                t.getNotes(),
                t.getStatus(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}