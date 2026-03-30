package com.orrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.orrs.model.SymptomAssessment;

public interface SymptomAssessmentRepository
        extends JpaRepository<SymptomAssessment, Long> {

    SymptomAssessment findBySessionId(Long sessionId);
}

