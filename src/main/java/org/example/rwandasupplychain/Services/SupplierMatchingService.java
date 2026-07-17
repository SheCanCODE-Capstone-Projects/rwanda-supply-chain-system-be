package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.SupplierMatchDtos.SupplierMatch;
import org.example.rwandasupplychain.Entities.Product;
import org.example.rwandasupplychain.Entities.RFQ;
import org.example.rwandasupplychain.Entities.SKU;
import org.example.rwandasupplychain.Entities.Users;
import org.example.rwandasupplychain.Enums.RfqStatus;
import org.example.rwandasupplychain.Repositories.ProductRepository;
import org.example.rwandasupplychain.Repositories.RFQRepository;
import org.example.rwandasupplychain.Repositories.SKURepository;
import org.example.rwandasupplychain.Repositories.UsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SupplierMatchingService {

    private static final double PRICE_WEIGHT = 0.5;
    private static final double PROXIMITY_WEIGHT = 0.5;
    private static final double MAX_RELEVANT_DISTANCE_KM = 300.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final ProductRepository productRepository;
    private final SKURepository skuRepository;
    private final UsersRepository usersRepository;
    private final RFQRepository rfqRepository;

    public SupplierMatchingService(ProductRepository productRepository,
                                    SKURepository skuRepository,
                                    UsersRepository usersRepository,
                                    RFQRepository rfqRepository) {
        this.productRepository = productRepository;
        this.skuRepository = skuRepository;
        this.usersRepository = usersRepository;
        this.rfqRepository = rfqRepository;
    }

    public List<RFQ> findOpenRfqsForSupplier(UUID supplierId) {
        Set<String> categories = productRepository.findByOrgId(supplierId).stream()
                .filter(Product::isActive)
                .map(Product::getCategory)
                .collect(Collectors.toSet());

        Map<UUID, RFQ> matches = new LinkedHashMap<>();
        for (String category : categories) {
            rfqRepository.findByStatusAndCategoryIgnoreCase(RfqStatus.OPEN, category)
                    .forEach(rfq -> matches.put(rfq.getId(), rfq));
        }
        return matches.values().stream()
                .sorted(Comparator.comparing(RFQ::getCreatedAt).reversed())
                .toList();
    }

    public List<SupplierMatch> findMatches(RFQ rfq, int limit) {
        List<Product> candidates = productRepository.findByCategoryIgnoreCaseAndActiveTrue(rfq.getCategory());
        Map<UUID, SupplierMatch> bestPerSupplier = new HashMap<>();

        for (Product product : candidates) {
            if (product.getOrgId().equals(rfq.getBuyerId())) {
                continue;
            }
            SKU cheapestSku = skuRepository.findByProduct_Id(product.getId()).stream()
                    .filter(SKU::isActive)
                    .min(Comparator.comparing(SKU::getPrice))
                    .orElse(null);
            if (cheapestSku == null) {
                continue;
            }

            SupplierMatch candidate = toMatch(rfq, product, cheapestSku);
            bestPerSupplier.merge(product.getOrgId(), candidate,
                    (existing, incoming) -> incoming.score() > existing.score() ? incoming : existing);
        }

        return bestPerSupplier.values().stream()
                .sorted(Comparator.comparingDouble(SupplierMatch::score).reversed())
                .limit(limit)
                .toList();
    }

    private SupplierMatch toMatch(RFQ rfq, Product product, SKU sku) {
        Users supplier = usersRepository.findById(product.getOrgId()).orElse(null);
        Double distanceKm = distanceKm(rfq, supplier);
        double priceScore = priceScore(sku.getPrice(), rfq.getTargetPrice());
        double proximityScore = proximityScore(distanceKm);
        double score = PRICE_WEIGHT * priceScore + PROXIMITY_WEIGHT * proximityScore;

        return new SupplierMatch(
                product.getOrgId(),
                supplier != null ? supplier.getFullName() : null,
                product.getId(),
                product.getName(),
                sku.getId(),
                sku.getSkuCode(),
                sku.getPrice(),
                distanceKm,
                score
        );
    }

    private double priceScore(BigDecimal price, BigDecimal targetPrice) {
        if (targetPrice == null || price == null || price.signum() <= 0) {
            return 0.5;
        }
        if (price.compareTo(targetPrice) <= 0) {
            return 1.0;
        }
        double ratio = targetPrice.doubleValue() / price.doubleValue();
        return clamp(ratio, 0.0, 1.0);
    }

    private double proximityScore(Double distanceKm) {
        if (distanceKm == null) {
            return 0.5;
        }
        return clamp(1.0 - (distanceKm / MAX_RELEVANT_DISTANCE_KM), 0.0, 1.0);
    }

    private Double distanceKm(RFQ rfq, Users supplier) {
        if (supplier == null
                || rfq.getDeliveryLatitude() == null || rfq.getDeliveryLongitude() == null
                || supplier.getLatitude() == null || supplier.getLongitude() == null) {
            return null;
        }
        return haversineKm(rfq.getDeliveryLatitude(), rfq.getDeliveryLongitude(),
                supplier.getLatitude(), supplier.getLongitude());
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
