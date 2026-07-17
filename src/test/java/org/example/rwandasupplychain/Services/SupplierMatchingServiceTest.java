package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.SupplierMatchDtos.SupplierMatch;
import org.example.rwandasupplychain.Entities.Product;
import org.example.rwandasupplychain.Entities.ProducerType;
import org.example.rwandasupplychain.Entities.RFQ;
import org.example.rwandasupplychain.Entities.SKU;
import org.example.rwandasupplychain.Entities.Users;
import org.example.rwandasupplychain.Repositories.ProductRepository;
import org.example.rwandasupplychain.Repositories.RFQRepository;
import org.example.rwandasupplychain.Repositories.SKURepository;
import org.example.rwandasupplychain.Repositories.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierMatchingServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private SKURepository skuRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private RFQRepository rfqRepository;

    private SupplierMatchingService matchingService;

    private UUID buyerId;
    private UUID nearSupplierId;
    private UUID farSupplierId;

    @BeforeEach
    void setUp() {
        matchingService = new SupplierMatchingService(productRepository, skuRepository, usersRepository, rfqRepository);
        buyerId = UUID.randomUUID();
        nearSupplierId = UUID.randomUUID();
        farSupplierId = UUID.randomUUID();
    }

    @Test
    void findMatches_ranksCloserCheaperSupplierHigher() {
        RFQ rfq = new RFQ();
        rfq.setBuyerId(buyerId);
        rfq.setCategory("Maize");
        rfq.setTargetPrice(BigDecimal.valueOf(300));
        rfq.setDeliveryLatitude(-1.9500);
        rfq.setDeliveryLongitude(30.0600);

        Product nearProduct = product(nearSupplierId, "Maize");
        Product farProduct = product(farSupplierId, "Maize");
        when(productRepository.findByCategoryIgnoreCaseAndActiveTrue("Maize"))
                .thenReturn(List.of(nearProduct, farProduct));

        when(skuRepository.findByProduct_Id(nearProduct.getId()))
                .thenReturn(List.of(sku(nearProduct, BigDecimal.valueOf(280))));
        when(skuRepository.findByProduct_Id(farProduct.getId()))
                .thenReturn(List.of(sku(farProduct, BigDecimal.valueOf(280))));

        Users nearSupplier = supplier(nearSupplierId, -1.9600, 30.0700);
        Users farSupplier = supplier(farSupplierId, -2.6000, 29.7400);
        when(usersRepository.findById(nearSupplierId)).thenReturn(Optional.of(nearSupplier));
        when(usersRepository.findById(farSupplierId)).thenReturn(Optional.of(farSupplier));

        List<SupplierMatch> matches = matchingService.findMatches(rfq, 10);

        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).supplierId()).isEqualTo(nearSupplierId);
        assertThat(matches.get(0).score()).isGreaterThan(matches.get(1).score());
    }

    @Test
    void findMatches_excludesBuyersOwnProducts() {
        RFQ rfq = new RFQ();
        rfq.setBuyerId(buyerId);
        rfq.setCategory("Maize");

        Product ownProduct = product(buyerId, "Maize");
        when(productRepository.findByCategoryIgnoreCaseAndActiveTrue("Maize")).thenReturn(List.of(ownProduct));

        List<SupplierMatch> matches = matchingService.findMatches(rfq, 10);

        assertThat(matches).isEmpty();
    }

    @Test
    void findMatches_skipsProductsWithNoActiveSku() {
        RFQ rfq = new RFQ();
        rfq.setBuyerId(buyerId);
        rfq.setCategory("Maize");

        Product product = product(nearSupplierId, "Maize");
        when(productRepository.findByCategoryIgnoreCaseAndActiveTrue("Maize")).thenReturn(List.of(product));
        when(skuRepository.findByProduct_Id(product.getId())).thenReturn(List.of());

        List<SupplierMatch> matches = matchingService.findMatches(rfq, 10);

        assertThat(matches).isEmpty();
    }

    private Product product(UUID orgId, String category) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setOrgId(orgId);
        product.setName(category);
        product.setCategory(category);
        product.setUnit("kg");
        product.setProducerType(ProducerType.PRODUCER);
        product.setActive(true);
        return product;
    }

    private SKU sku(Product product, BigDecimal price) {
        SKU sku = new SKU();
        sku.setId(UUID.randomUUID());
        sku.setProduct(product);
        sku.setSkuCode(product.getName() + "-001");
        sku.setPrice(price);
        sku.setUnit(product.getUnit());
        sku.setActive(true);
        return sku;
    }

    private Users supplier(UUID id, double lat, double lon) {
        Users user = new Users();
        user.setId(id);
        user.setFullName("Supplier " + id);
        user.setLatitude(lat);
        user.setLongitude(lon);
        return user;
    }
}
