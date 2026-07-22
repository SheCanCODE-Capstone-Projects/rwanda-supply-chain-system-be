package org.example.rwandasupplychain.Entities.FarmerEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    // Total storage capacity of the warehouse (units consistent with the product's unit).
    @Column(nullable = false)
    private Integer capacity;

    // Capacity still free to be booked. Decremented/restored as bookings are made/cancelled.
    @Column(name = "available_capacity", nullable = false)
    private Integer availableCapacity;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.availableCapacity == null) {
            this.availableCapacity = this.capacity;
        }
    }
}