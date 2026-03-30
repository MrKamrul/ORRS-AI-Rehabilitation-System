package com.orrs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.orrs.model.RehabPlan;

import java.util.List;

public interface RehabPlanRepository
        extends JpaRepository<RehabPlan, Long> {

    List<RehabPlan> findByPatientId(Long patientId);

    RehabPlan findTopByPatientIdOrderByPlanStartDateDesc(Long patientId);

    RehabPlan findTopBySessionIdOrderByIdDesc(Long sessionId);
}
