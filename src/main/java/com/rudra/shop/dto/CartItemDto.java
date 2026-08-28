package com.rudra.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String name;
    private String imageUrl;
    private Double price;
    private int quantity;

    public Double getTotalPrice() {
        return price != null ? price * quantity : 0.0;
    }
}
