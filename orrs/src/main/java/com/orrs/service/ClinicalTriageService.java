package com.orrs.service;

import com.orrs.model.DexterityAssessment;
import com.orrs.model.GaitAssessment;
import com.orrs.model.SymptomAssessment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ClinicalTriageService {

    /**
     * Simple safety gate for Phase-3.
     * In real clinical use, rules would be validated and expanded.
     */
    public Map<String, Object> triage(GaitAssessment gait, DexterityAssessment dex, SymptomAssessment symptom) {
        Map<String, Object> res = new HashMap<>();
        boolean needsReview = false;
        String reason = null;

        // Severe neuropathy
        if (symptom.getOverallNeuropathyScore() != null && symptom.getOverallNeuropathyScore() >= 8) {
            needsReview = true;
            reason = "Severe neuropathy score - doctor review required";
        }

        // High fall risk (TUG > 13.5s is commonly used fall-risk screening threshold)
        if (!needsReview && gait.getTugTimeSeconds() != null && gait.getTugTimeSeconds() > 13.5) {
            needsReview = true;
            reason = "High fall risk from gait test (TUG)";
        }

        res.put("safeForHomeRehab", !needsReview);
        res.put("requiresDoctorReview", needsReview);
        res.put("reason", reason);
        return res;
    }
}
