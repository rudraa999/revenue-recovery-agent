package com.rudra.shop.repository;

import com.rudra.shop.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    List<Notification> findByUserIdOrSessionIdOrderByCreatedAtDesc(Long userId, String sessionId);
    long countByUserIdAndIsReadFalse(Long userId);
    long countBySessionIdAndIsReadFalse(String sessionId);
    boolean existsBySessionIdAndType(String sessionId, String type);
    boolean existsByUserIdAndType(Long userId, String type);
}
