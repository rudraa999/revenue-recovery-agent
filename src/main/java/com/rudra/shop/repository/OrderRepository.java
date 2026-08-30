package com.rudra.shop.repository;

import com.rudra.shop.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
    List<Order> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);
    List<Order> findByStatusOrderByCreatedAtDesc(String status);
    List<Order> findAllByOrderByCreatedAtDesc();

    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);
    Page<Order> findByStatus(String status, Pageable pageable);
    Page<Order> findAll(Pageable pageable);

    boolean existsByCustomerEmailAndStatusAndCreatedAtAfter(String customerEmail, String status, java.time.LocalDateTime createdAt);
    boolean existsByCustomerPhoneAndStatusAndCreatedAtAfter(String customerPhone, String status, java.time.LocalDateTime createdAt);
    boolean existsByUserIdAndStatusAndCreatedAtAfter(Long userId, String status, java.time.LocalDateTime createdAt);
    boolean existsByCustomerEmailAndStatusAndOrderNumberNot(String customerEmail, String status, String orderNumber);
    boolean existsByCustomerPhoneAndStatusAndOrderNumberNot(String customerPhone, String status, String orderNumber);
}
