package com.rudra.shop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
@Data
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // e.g. "WELCOME10", "WINBACK10-XXXX"

    private String description;

    @Column(nullable = false)
    private String discountType = "PERCENTAGE"; // "PERCENTAGE" or "FIXED"

    @Column(nullable = false)
    private Double discountValue; // e.g. 10.0 for 10% or 100.0 for ₹100

    private Double minOrderAmount = 0.0; // Minimum order value required to apply

    private Double maxDiscountAmount; // Cap on discount for percentage-based promos (e.g. max ₹2000)

    private LocalDateTime expiryDate;

    private Integer maxUses = 1000;

    private Integer usedCount = 0;

    private boolean active = true;

    private boolean recoveryExclusive = false; // If true, created dynamically for a specific recovery order

    private String targetOrderNumber; // Optional: specific order number for exclusive recovery coupons

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
