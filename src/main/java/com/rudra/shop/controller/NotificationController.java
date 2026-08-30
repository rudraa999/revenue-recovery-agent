package com.rudra.shop.controller;

import com.rudra.shop.model.Notification;
import com.rudra.shop.model.User;
import com.rudra.shop.repository.UserRepository;
import com.rudra.shop.service.CartSessionService;
import com.rudra.shop.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartSessionService cartSessionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(HttpSession session, Authentication authentication) {
        Long userId = getUserId(authentication);
        String sessionId = session.getId();

        // Check if user has active items in cart and auto-trigger retention notification
        int cartCount = cartSessionService.getCartCount(session);
        if (cartCount > 0) {
            double total = cartSessionService.getCartTotal(session);
            notificationService.checkAndTriggerAbandonedCartNotification(userId, sessionId, cartCount, total);
        }

        List<Notification> notifications = notificationService.getNotificationsForUserOrSession(userId, sessionId);
        long unreadCount = notificationService.getUnreadCount(userId, sessionId);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark-read/{id}")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("id") Long id) {
        notificationService.markAsRead(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session, Authentication authentication) {
        Long userId = getUserId(authentication);
        notificationService.markAllAsReadForSession(userId, session.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            User user = userRepository.findByUsername(authentication.getName());
            if (user != null) {
                return user.getId();
            }
        }
        return null;
    }
}
