package com.rudra.shop.service;

import com.rudra.shop.dto.CartItemDto;
import com.rudra.shop.model.Cart;
import com.rudra.shop.model.CartItem;
import com.rudra.shop.model.Product;
import com.rudra.shop.repository.CartItemRepository;
import com.rudra.shop.repository.CartRepository;
import com.rudra.shop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartSessionService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Cart getOrCreateDatabaseCart(HttpSession session) {
        String sessionId = session.getId();
        return cartRepository.findBySessionId(sessionId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setSessionId(sessionId);
            return cartRepository.save(newCart);
        });
    }

    @Transactional(readOnly = true)
    public List<CartItemDto> getCart(HttpSession session) {
        Cart cart = getOrCreateDatabaseCart(session);
        return cart.getItems().stream()
                .map(item -> new CartItemDto(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getPrice(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addToCart(HttpSession session, Long productId, int quantity) {
        Cart cart = getOrCreateDatabaseCart(session);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            double price = parsePrice(product.getPrice());

            Optional<CartItem> existingItemOpt = cart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(productId))
                    .findFirst();

            if (existingItemOpt.isPresent()) {
                CartItem existingItem = existingItemOpt.get();
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                cartItemRepository.save(existingItem);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProduct(product);
                newItem.setQuantity(quantity);
                newItem.setPrice(price);
                cart.getItems().add(newItem);
                cartItemRepository.save(newItem);
            }
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void updateQuantity(HttpSession session, Long productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(session, productId);
            return;
        }
        Cart cart = getOrCreateDatabaseCart(session);
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    cartItemRepository.save(item);
                });
    }

    @Transactional
    public void removeFromCart(HttpSession session, Long productId) {
        Cart cart = getOrCreateDatabaseCart(session);
        List<CartItem> itemsToRemove = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .collect(Collectors.toList());

        for (CartItem item : itemsToRemove) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(HttpSession session) {
        Cart cart = getOrCreateDatabaseCart(session);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public int getCartCount(HttpSession session) {
        Cart cart = getOrCreateDatabaseCart(session);
        return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Transactional(readOnly = true)
    public double getCartTotal(HttpSession session) {
        Cart cart = getOrCreateDatabaseCart(session);
        return cart.getItems().stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    private double parsePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return 0.0;
        }
        try {
            String cleaned = priceStr.replaceAll("[^0-9.]", "");
            if (cleaned.isEmpty()) return 0.0;
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
