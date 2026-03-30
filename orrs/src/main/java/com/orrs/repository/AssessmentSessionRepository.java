package com.orrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.orrs.model.AssessmentSession;

import java.util.List;

public interface AssessmentSessionRepository
        extends JpaRepository<AssessmentSession, Long> {

    List<AssessmentSession> findByPatientId(Long patientId);

    java.util.List<AssessmentSession> findByPatientIdOrderByAssessmentDateDesc(Long patientId);

    AssessmentSession findTopByPatientIdOrderByAssessmentDateDesc(Long patientId);
}

