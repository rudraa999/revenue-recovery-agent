package com.rudra.shop.repository;

import com.rudra.shop.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeIgnoreCaseAndActiveTrue(String code);
    Optional<PromoCode> findByCodeIgnoreCase(String code);
    List<PromoCode> findByTargetOrderNumber(String targetOrderNumber);
    List<PromoCode> findAllByOrderByCreatedAtDesc();
}
