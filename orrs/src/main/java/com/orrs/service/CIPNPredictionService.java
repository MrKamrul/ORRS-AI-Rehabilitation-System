package com.orrs.service;

import com.orrs.model.AssessmentSession;
import com.orrs.model.CIPNPrediction;
import com.orrs.model.SymptomAssessment;
import com.orrs.repository.CIPNPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CIPNPredictionService {

    private final CIPNPredictionRepository predictionRepository;
    private final AiClientService aiClientService;

    /**
     * Phase-3: AI-first with safe fallback.
     * - If AI microservice is running, use its result.
     * - Otherwise fallback to rule-based baseline.
     */
    public CIPNPrediction generatePrediction(AssessmentSession session, SymptomAssessment symptom) {

        int neuropathy = safe(symptom.getOverallNeuropathyScore());
        int pain = safe(symptom.getPainScore());
        int numb = safe(symptom.getNumbnessScore());

        // Try AI service
        AiClientService.AiCipnRequest req = new AiClientService.AiCipnRequest();
        req.setNeuropathyScore(neuropathy);
        req.setPainScore(pain);
        req.setNumbnessScore(numb);
        // (Optional) if gait exists, pass TUG from elsewhere; kept null here

        AiClientService.AiCipnResponse ai = aiClientService.predictCipn(req);

        CIPNPrediction p = new CIPNPrediction();
        p.setSession(session);

        if (ai != null) {
            p.setSeverity(CIPNPrediction.CIPNSeverity.valueOf(ai.getSeverity()));
            p.setCipnLabel(ai.getLabel());
            p.setConfidenceScore(ai.getConfidence());
            p.setModelName("FastAPIPrototype");
            p.setModelVersion("0.1.0");
        } else {
            // Safe fallback
            int total = neuropathy + pain + numb;

            CIPNPrediction.CIPNSeverity severity;
            if (total == 0) severity = CIPNPrediction.CIPNSeverity.NONE;
            else if (total <= 6) severity = CIPNPrediction.CIPNSeverity.MILD;
            else if (total <= 14) severity = CIPNPrediction.CIPNSeverity.MODERATE;
            else severity = CIPNPrediction.CIPNSeverity.SEVERE;

            int label = (severity == CIPNPrediction.CIPNSeverity.SEVERE || severity == CIPNPrediction.CIPNSeverity.MODERATE) ? 1 : 0;

            p.setSeverity(severity);
            p.setCipnLabel(label);
            p.setConfidenceScore(Math.min(0.95, 0.50 + (total / 30.0)));
            p.setModelName("RuleBasedBaseline");
            p.setModelVersion("v1");
        }

        return predictionRepository.save(p);
    }

    private int safe(Integer v) { return v == null ? 0 : v; }
}
