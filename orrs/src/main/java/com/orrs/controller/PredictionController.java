package com.orrs.controller;

import com.orrs.dto.PredictionResponseDTO;
import com.orrs.model.AssessmentSession;
import com.orrs.model.CIPNPrediction;
import com.orrs.model.SymptomAssessment;
import com.orrs.repository.AssessmentSessionRepository;
import com.orrs.repository.SymptomAssessmentRepository;
import com.orrs.service.CIPNPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prediction")
@RequiredArgsConstructor
public class PredictionController {

    private final AssessmentSessionRepository sessionRepository;
    private final SymptomAssessmentRepository symptomRepository;
    private final CIPNPredictionService predictionService;

    @GetMapping("/run/{sessionId}")
    public PredictionResponseDTO run(@PathVariable Long sessionId) {
        AssessmentSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        SymptomAssessment symptom = symptomRepository.findBySessionId(sessionId);
        if (symptom == null) throw new IllegalArgumentException("Symptom assessment not found for session");

        CIPNPrediction p = predictionService.generatePrediction(session, symptom);

        PredictionResponseDTO dto = new PredictionResponseDTO();
        dto.setCipnLabel(p.getCipnLabel());
        dto.setSeverity(p.getSeverity().name());
        dto.setConfidenceScore(p.getConfidenceScore());
        dto.setRehabPlanSummary("Use /api/rehab/plan/" + sessionId + " to generate plan.");
        return dto;
    }
}
