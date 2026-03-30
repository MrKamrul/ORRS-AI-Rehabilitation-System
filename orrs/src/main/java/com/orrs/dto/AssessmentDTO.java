package com.orrs.dto;

import lombok.Data;

@Data
public class AssessmentDTO {

    // Session info
    private Long sessionId;

    // Gait
    private Double tugTimeSeconds;
    private Boolean balanceIssueObserved;

    // Dexterity
    private Integer completionTimeSeconds;
    private Integer errorCount;
    private String handUsed;

    // Symptoms
    private Integer neuropathyScore;
    private Integer painScore;
    private Integer numbnessScore;
}
