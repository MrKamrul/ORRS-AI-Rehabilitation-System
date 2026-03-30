package com.orrs.repository;

import com.orrs.model.RehabPlanWeek;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RehabPlanWeekRepository extends JpaRepository<RehabPlanWeek, Long> {
    RehabPlanWeek findTopByRehabPlanIdOrderByIdDesc(Long rehabPlanId);
}
