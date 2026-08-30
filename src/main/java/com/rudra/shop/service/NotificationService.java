package com.rudra.shop.service;

import com.rudra.shop.model.Notification;
import com.rudra.shop.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getNotificationsForUserOrSession(Long userId, String sessionId) {
        if (userId != null) {
            return notificationRepository.findByUserIdOrSessionIdOrderByCreatedAtDesc(userId, sessionId);
        } else if (sessionId != null) {
            return notificationRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
        }
        return List.of();
    }

    public long getUnreadCount(Long userId, String sessionId) {
        if (userId != null) {
            return notificationRepository.countByUserIdAndIsReadFalse(userId);
        } else if (sessionId != null) {
            return notificationRepository.countBySessionIdAndIsReadFalse(sessionId);
        }
        return 0;
    }

    @Transactional
    public Notification createNotification(Long userId, String sessionId, String title, String message, String type, String actionUrl, String badgeText) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setSessionId(sessionId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type != null ? type : "ABANDONED_CART");
        notification.setActionUrl(actionUrl != null ? actionUrl : "/cart");
        notification.setBadgeText(badgeText != null ? badgeText : "Offer");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAsRead(Long id) {
        Optional<Notification> notifOpt = notificationRepository.findById(id);
        notifOpt.ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsReadForSession(Long userId, String sessionId) {
        List<Notification> list = getNotificationsForUserOrSession(userId, sessionId);
        for (Notification notif : list) {
            notif.setRead(true);
        }
        notificationRepository.saveAll(list);
    }

    @Transactional
    public void checkAndTriggerAbandonedCartNotification(Long userId, String sessionId, int itemCount, double cartTotal) {
        if (itemCount <= 0 || cartTotal <= 0) {
            return;
        }

        // Avoid duplicate spam notifications for the same session
        if (sessionId != null && notificationRepository.existsBySessionIdAndType(sessionId, "ABANDONED_CART")) {
            return;
        }
        if (userId != null && notificationRepository.existsByUserIdAndType(userId, "ABANDONED_CART")) {
            return;
        }

        String offerTitle = "⚡ Special Cart Offer Reserved!";
        String offerMessage = "You left " + itemCount + " handcrafted items in your cart (₹" + String.format("%.2f", cartTotal) + "). Complete your order now to get Free Express Delivery & a surprise gift!";

        createNotification(userId, sessionId, offerTitle, offerMessage, "ABANDONED_CART", "/checkout", "Special Offer");
    }
}
