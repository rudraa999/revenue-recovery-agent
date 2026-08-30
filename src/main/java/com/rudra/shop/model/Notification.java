package com.rudra.shop.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop_notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Optional: user ID if logged in
    private String sessionId; // Optional: session ID for guest visitors

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    private String type = "ABANDONED_CART"; // ABANDONED_CART, PROMO_OFFER, PAYMENT_RECOVERY, SYSTEM

    private String actionUrl = "/cart";

    private String badgeText = "Offer";

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
