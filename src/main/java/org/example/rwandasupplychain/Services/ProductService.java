package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.ProductDtos.ProductRequest;
import org.example.rwandasupplychain.DTOs.ProductDtos.ProductResponse;
import org.example.rwandasupplychain.Entities.Product;
import org.example.rwandasupplychain.Exceptions.DuplicateResourceException;
import org.example.rwandasupplychain.Exceptions.ResourceNotFoundException;
import org.example.rwandasupplychain.Repositories.ProductRepository;
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

    protected Product findEntity(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setOrgId(request.orgId());
        product.setName(request.name());
        product.setCategory(request.category());
        product.setUnit(request.unit());
        product.setDescription(request.description());
        product.setProducerType(request.producerType());
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
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}