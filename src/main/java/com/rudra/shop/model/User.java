package com.rudra.shop.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    private String role;

    private String provider = "LOCAL";

    private String otp;
    private java.time.LocalDateTime otpGeneratedTime;
    private boolean verified = false;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Wishlist> wishlists = new java.util.ArrayList<>();
}