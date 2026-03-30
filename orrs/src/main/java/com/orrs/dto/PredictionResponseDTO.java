package com.orrs.dto;

import lombok.Data;

@Data
public class PredictionResponseDTO {

    private Integer cipnLabel; // 0 = no CIPN, 1 = CIPN
    private String severity;   // NONE, MILD, MODERATE, SEVERE
    private Double confidenceScore;
    private String rehabPlanSummary;
}
