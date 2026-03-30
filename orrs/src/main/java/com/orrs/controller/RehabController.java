package com.orrs.controller;

import com.orrs.model.*;
import com.orrs.repository.AssessmentSessionRepository;
import com.orrs.repository.DexterityAssessmentRepository;
import com.orrs.repository.GaitAssessmentRepository;
import com.orrs.repository.SymptomAssessmentRepository;
import com.orrs.repository.RehabPlanRepository;
import com.orrs.repository.RehabPlanWeekRepository;
import com.orrs.service.ClinicalTriageService;
import com.orrs.service.RehabService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rehab")
@RequiredArgsConstructor
public class RehabController {

    private final AssessmentSessionRepository sessionRepository;
    private final GaitAssessmentRepository gaitRepository;
    private final DexterityAssessmentRepository dexRepository;
    private final SymptomAssessmentRepository symptomRepository;

    private final ClinicalTriageService triageService;
    private final RehabService rehabService;
    private final RehabPlanRepository rehabPlanRepository;
    private final RehabPlanWeekRepository rehabPlanWeekRepository;

    @GetMapping("/plan/{sessionId}")
    public RehabPlan generate(@PathVariable Long sessionId) {
        AssessmentSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        GaitAssessment gait = gaitRepository.findBySessionId(sessionId);
        DexterityAssessment dex = dexRepository.findBySessionId(sessionId);
        SymptomAssessment symptom = symptomRepository.findBySessionId(sessionId);

        if (gait == null || dex == null || symptom == null) {
            throw new IllegalArgumentException("Missing assessment components for session");
        }

        boolean safe = (boolean) triageService.triage(gait, dex, symptom).get("safeForHomeRehab");
        return rehabService.generateRehabPlan(session, symptom, gait, dex, safe);
    }


@GetMapping("/weekly/{sessionId}")
public Object getWeeklyPlan(@PathVariable Long sessionId) {
    RehabPlan plan = rehabPlanRepository.findTopBySessionIdOrderByIdDesc(sessionId);
    if (plan == null) throw new IllegalArgumentException("Rehab plan not found for session");
    RehabPlanWeek week = rehabPlanWeekRepository.findTopByRehabPlanIdOrderByIdDesc(plan.getId());
    if (week == null) return java.util.Map.of("message", "Weekly plan not generated yet.");
    return java.util.Map.of(
            "rehabPlanId", plan.getId(),
            "weekStart", week.getWeekStart(),
            "weekJson", week.getWeekJson()
    );
}

}
