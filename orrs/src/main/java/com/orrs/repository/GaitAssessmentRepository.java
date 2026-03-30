package com.orrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.orrs.model.GaitAssessment;

public interface GaitAssessmentRepository
        extends JpaRepository<GaitAssessment, Long> {

    GaitAssessment findBySessionId(Long sessionId);
}


