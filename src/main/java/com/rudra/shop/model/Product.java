package com.rudra.shop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String price;
    private String category;
    private String subcategory;
    @Column(columnDefinition = "TEXT")
    private String imageUrls;

    private boolean trending = false;
    private boolean inCollection = false;
    private Integer displayOrder = 0;

    public String getImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String[] urls = imageUrls.split(",");
            if (urls.length > 0) {
                return urls[0].trim();
            }
        }
        return null;
    }

    private boolean deleted = false;
}