package com.orrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.orrs.model.CIPNPrediction;

public interface CIPNPredictionRepository
        extends JpaRepository<CIPNPrediction, Long> {

    CIPNPrediction findBySessionId(Long sessionId);
}
