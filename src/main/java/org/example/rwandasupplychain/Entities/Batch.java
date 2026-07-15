package org.example.rwandasupplychain.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "batches")
@Getter
@Setter
@NoArgsConstructor
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sku_id", nullable = false)
    private SKU sku;

    @Column(name = "batch_no", nullable = false)
    private String batchNo;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
}