package com.rudra.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rudra.shop.model.Product;
import com.rudra.shop.model.User;
import com.rudra.shop.model.Wishlist;
import com.rudra.shop.repository.ProductRepository;
import com.rudra.shop.repository.UserRepository;
import com.rudra.shop.repository.WishlistRepository;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Wishlist> getWishlistForUser(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
        }
        if (user != null) {
            return wishlistRepository.findByUser(user);
        }
        return List.of();
    }

    @Transactional
    public boolean toggleWishlist(String usernameOrEmail, Long productId) {
        User user = userRepository.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
        }
        if (user == null)
            return false;

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty())
            return false;

        Product product = productOpt.get();

        Optional<Wishlist> existing = wishlistRepository.findByUserAndProduct(user, product);
        if (existing.isPresent()) {
            wishlistRepository.deleteByUserAndProduct(user, product);
            return false; // Removed from wishlist
        } else {
            Wishlist newWishlist = new Wishlist();
            newWishlist.setUser(user);
            newWishlist.setProduct(product);
            wishlistRepository.save(newWishlist);
            return true; // Added to wishlist
        }
    }

    public Long getWishlistCount(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
        }
        if (user != null) {
            return wishlistRepository.countByUser(user);
        }
        return 0L;
    }
}
