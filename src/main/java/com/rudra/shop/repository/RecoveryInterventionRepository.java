package com.rudra.shop.repository;

import com.rudra.shop.model.RecoveryIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecoveryInterventionRepository extends JpaRepository<RecoveryIntervention, Long> {
    List<RecoveryIntervention> findByRiskRecordIdOrderByCreatedAtDesc(Long riskRecordId);
    List<RecoveryIntervention> findAllByOrderByCreatedAtDesc();
}
