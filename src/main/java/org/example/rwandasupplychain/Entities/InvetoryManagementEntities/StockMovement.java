package org.example.rwandasupplychain.Entities.InvetoryManagementEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String reason;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "movement_date", nullable = false, updatable = false)
    private LocalDateTime movementDate;

    @PrePersist
    protected void onCreate() {
        this.movementDate = LocalDateTime.now();
    }
}