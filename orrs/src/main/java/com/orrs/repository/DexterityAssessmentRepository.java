package com.orrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.orrs.model.DexterityAssessment;

public interface DexterityAssessmentRepository
        extends JpaRepository<DexterityAssessment, Long> {

    DexterityAssessment findBySessionId(Long sessionId);
}

