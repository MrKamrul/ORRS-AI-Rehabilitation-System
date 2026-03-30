package com.orrs.service;

import com.orrs.model.*;
import com.orrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionDetailService {

    private final AssessmentSessionRepository sessionRepo;
    private final SymptomAssessmentRepository symptomRepo;
    private final GaitAssessmentRepository gaitRepo;
    private final DexterityAssessmentRepository dexRepo;
    private final CIPNPredictionRepository predictionRepo;
    private final RehabPlanRepository rehabRepo;

    public Map<String, Object> details(Long sessionId) {
        AssessmentSession session = sessionRepo.findById(sessionId).orElseThrow();
        SymptomAssessment s = symptomRepo.findBySessionId(sessionId);
        GaitAssessment g = gaitRepo.findBySessionId(sessionId);
        DexterityAssessment d = dexRepo.findBySessionId(sessionId);
        CIPNPrediction p = predictionRepo.findBySessionId(sessionId);

        RehabPlan plan = rehabRepo.findAll().stream()
                .filter(r -> r.getSession() != null && sessionId.equals(r.getSession().getId()))
                .findFirst().orElse(null);

        Map<String, Object> out = new HashMap<>();
        out.put("session", session);
        out.put("symptom", s);
        out.put("gait", g);
        out.put("dexterity", d);
        out.put("prediction", p);
        out.put("rehabPlan", plan);
        return out;
    }
}
