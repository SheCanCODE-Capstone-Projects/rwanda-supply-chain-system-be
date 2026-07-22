package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.FarmerDtos.ProductDtos.ProductRequest;
import org.example.rwandasupplychain.DTOs.FarmerDtos.ProductDtos.ProductResponse;
import org.example.rwandasupplychain.Entities.FarmerEntities.Product;
import org.example.rwandasupplychain.Exceptions.DuplicateResourceException;
import org.example.rwandasupplychain.Exceptions.ForbiddenOperationException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.FarmerRepositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsByNameIgnoreCaseAndOrgId(request.name(), request.orgId())) {
            throw new DuplicateResourceException("A product named '" + request.name() + "' already exists for this organization");
        }
        Product product = new Product();
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(UUID orgId) {
        List<Product> products = orgId == null ? productRepository.findAll() : productRepository.findByOrgId(orgId);
        return products.stream().map(this::toResponse).toList();
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = findEntity(id);
        applyRequest(product, request);
        return toResponse(productRepository.save(product));
    }

    public void delete(UUID id) {
        productRepository.delete(findEntity(id));
    }

    public Product findEntity(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }



    public ProductResponse createForFarmer(UUID farmerId, ProductRequest request) {
        if (productRepository.existsByNameIgnoreCaseAndOrgId(request.name(), farmerId)) {
            throw new DuplicateResourceException("A product named '" + request.name() + "' already exists for this organization");
        }
        Product product = new Product();
        applyRequest(product, request);
        product.setOrgId(farmerId);
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllForFarmer(UUID farmerId) {
        return productRepository.findByOrgId(farmerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getByIdForFarmer(UUID farmerId, UUID id) {
        return toResponse(findOwnedEntity(farmerId, id));
    }

    public ProductResponse updateForFarmer(UUID farmerId, UUID id, ProductRequest request) {
        Product product = findOwnedEntity(farmerId, id);
        applyRequest(product, request);
        product.setOrgId(farmerId);
        return toResponse(productRepository.save(product));
    }

    public void deleteForFarmer(UUID farmerId, UUID id) {
        productRepository.delete(findOwnedEntity(farmerId, id));
    }

    private Product findOwnedEntity(UUID farmerId, UUID id) {
        Product product = findEntity(id);
        if (!product.getOrgId().equals(farmerId)) {
            throw new ForbiddenOperationException("You do not have permission to access this product");
        }
        return product;
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setOrgId(request.orgId());
        product.setName(request.name());
        product.setCategory(request.category());
        product.setUnit(request.unit());
        product.setDescription(request.description());
        product.setProducerType(request.producerType());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        product.setBatch(request.batch());
        product.setStatus(request.status());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getOrgId(),
                product.getName(),
                product.getCategory(),
                product.getUnit(),
                product.getDescription(),
                product.getProducerType(),
                product.isActive(),
                product.getPrice(),
                product.getQuantity(),
                product.getBatch(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}