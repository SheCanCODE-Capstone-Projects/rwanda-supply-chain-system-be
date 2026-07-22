package org.example.rwandasupplychain.Entities.InvetoryManagementEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.rwandasupplychain.Entities.FarmerEntities.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "skus")
@Getter
@Setter
@NoArgsConstructor
public class SKU {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String unit;

    @Column(unique = true)
    private String barcode;

    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 10;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "sku", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Batch> batches = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.barcode == null) {
            this.barcode = this.skuCode;
        }
    }
}