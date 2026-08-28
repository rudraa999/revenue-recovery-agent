package com.rudra.shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rudra.shop.model.Wishlist;
import com.rudra.shop.service.WishlistService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    private String getUsernameFromAuthentication(Authentication authentication) {
        if (authentication == null)
            return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @GetMapping("/wishlist")
    public String viewWishlist(Model model, Authentication authentication) {
        String username = getUsernameFromAuthentication(authentication);
        if (username != null) {
            List<Wishlist> wishlists = wishlistService.getWishlistForUser(username);
            model.addAttribute("wishlists", wishlists);
            return "wishlist";
        }
        return "redirect:/login";
    }

    @PostMapping("/wishlist/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestParam Long productId,
            Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        String username = getUsernameFromAuthentication(authentication);

        if (username == null) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean isAdded = wishlistService.toggleWishlist(username, productId);
            Long count = wishlistService.getWishlistCount(username);
            response.put("success", true);
            response.put("isAdded", isAdded);
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error modifying wishlist");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/wishlist/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistCount(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        String username = getUsernameFromAuthentication(authentication);

        if (username != null) {
            Long count = wishlistService.getWishlistCount(username);
            response.put("count", count);
        } else {
            response.put("count", 0);
        }
        return ResponseEntity.ok(response);
    }
}
