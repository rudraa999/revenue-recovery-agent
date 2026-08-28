package com.rudra.shop.controller;

import com.rudra.shop.dto.CartItemDto;
import com.rudra.shop.service.CartSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    @Autowired
    private CartSessionService cartSessionService;

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        List<CartItemDto> cartItems = cartSessionService.getCart(session);
        double total = cartSessionService.getCartTotal(session);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartCount", cartSessionService.getCartCount(session));

        return "cart";
    }

    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            HttpSession session) {

        cartSessionService.addToCart(session, productId, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cartCount", cartSessionService.getCartCount(session));
        response.put("cartTotal", cartSessionService.getCartTotal(session));
        response.put("message", "Product added to cart!");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartQuantity(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            HttpSession session) {

        cartSessionService.updateQuantity(session, productId, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cartCount", cartSessionService.getCartCount(session));
        response.put("cartTotal", cartSessionService.getCartTotal(session));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/cart/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @RequestParam("productId") Long productId,
            HttpSession session) {

        cartSessionService.removeFromCart(session, productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cartCount", cartSessionService.getCartCount(session));
        response.put("cartTotal", cartSessionService.getCartTotal(session));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/cart/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("count", cartSessionService.getCartCount(session));
        response.put("total", cartSessionService.getCartTotal(session));
        return ResponseEntity.ok(response);
    }
}
