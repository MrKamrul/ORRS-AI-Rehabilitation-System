package com.orrs.service;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiClientService {

    private static final Logger log = LoggerFactory.getLogger(AiClientService.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AiClientService(RestTemplate restTemplate,
                           @Value("${app.ai.baseUrl:http://127.0.0.1:8001}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public AiCipnResponse predictCipn(AiCipnRequest req) {
        final String url = baseUrl + "/ai/cipn/predict";
        log.info("[AI] -> POST {}", url);
        log.debug("[AI] CIPN request: {}", req);
        try {
            AiCipnResponse res = restTemplate.postForObject(url, req, AiCipnResponse.class);
            if (res == null) {
                log.warn("[AI] <- CIPN response: null (fallback to rule engine)");
                return null;
            }
            log.info("[AI] <- CIPN ok severity={} ctcaeGrade={} riskProb14d={} trend={} conf={}",
                    res.getSeverity(), res.getCtcaeGrade(), res.getRiskProb14d(), res.getTrend(), res.getConfidence());
            log.debug("[AI] CIPN full response: {}", res);
            return res;
        } catch (RestClientException ex) {
            log.error("[AI] CIPN call failed: {}", ex.getMessage(), ex);
            return null; // fallback to rule-based
        }
    }

    public AiRehabWeeklyResponse generateWeeklyRehabPlan(AiRehabWeeklyRequest req) {
        final String url = baseUrl + "/ai/rehab/weekly";
        log.info("[AI] -> POST {}", url);
        log.debug("[AI] Rehab request: {}", req);
        try {
            AiRehabWeeklyResponse res = restTemplate.postForObject(url, req, AiRehabWeeklyResponse.class);
            if (res == null) {
                log.warn("[AI] <- Rehab weekly response: null (fallback to rule engine)");
                return null;
            }
            int days = (res.getWeekPlan() == null) ? 0 : res.getWeekPlan().size();
            log.info("[AI] <- Rehab weekly ok level={} days={} doctorApprovalRequired={}",
                    res.getRehabLevel(), days, res.isDoctorApprovalRequired());
            log.debug("[AI] Rehab weekly full response: {}", res);
            return res;
        } catch (RestClientException ex) {
            log.error("[AI] Rehab weekly call failed: {}", ex.getMessage(), ex);
            return null;
        }
    }

    // ---------------- DTOs ----------------

    @Data
    public static class AiCipnRequest {
        private int neuropathyScore;
        private int painScore;
        private int numbnessScore;
        private Double tugTimeSeconds;

        // enrichment (optional)
        private Integer age;
        private Boolean baselineNeuropathy;
        private Boolean hasDiabetes;
        private String chemoAgents;

        // for trend calc
        private Integer lastTotalScore;
    }

    @Data
    public static class AiCipnResponse {
        private String severity;
        private int label;
        private double confidence;
        private List<String> explanation;

        // expanded fields
        private Integer ctcaeGrade;
        private Double riskProb14d;
        private String trend;
        private List<String> topFactors;
    }

    @Data
    public static class AiRehabWeeklyRequest {
        // Patient / cancer context
        private String fullName;
        private Integer age;
        private String gender;

        private String cancerType;
        private String cancerStage;
        private String primarySite;

        private String treatmentType;
        private String chemoAgents;
        private String radiationSite;
        private Boolean surgeryPerformed;

        private String dominantHand;
        private String comorbidities;
        private String activityLevel;

        private Integer heightCm;
        private Double weightBeforeTreatment;
        private Double weightAfterTreatment;

        // assessment snapshot
        private Integer ctcaeGrade;
        private String fallRisk;
        private Double tugTimeSeconds;
        private Double completionTimeSeconds;
        private Integer errorCount;
        private String handUsed;

        private Integer neuropathyScore;
        private Integer painScore;
        private Integer numbnessScore;
    }

    @Data
    public static class AiRehabWeeklyResponse {
        private int schemaVersion;
        private String rehabLevel;
        private boolean doctorApprovalRequired;
        private List<String> safetyNotes;
        private String weekStart;
        private List<Map<String, Object>> weekPlan;
    }
}
