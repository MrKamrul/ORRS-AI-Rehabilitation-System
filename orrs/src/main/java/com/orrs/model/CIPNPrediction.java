package com.orrs.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "cipn_predictions")
public class CIPNPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="session_id")
    @JsonIgnore
    private AssessmentSession session;

    // Binary or multi-class label
    // 0 = No CIPN, 1 = CIPN Present
    private Integer cipnLabel;

    @Enumerated(EnumType.STRING)
    private CIPNSeverity severity;

    // ML confidence (0–1)
    private Double confidenceScore;

    @Column(length = 2000)
    private String explanation;

    private String modelName;
    private String modelVersion;

    public enum CIPNSeverity {
        NONE,
        MILD,
        MODERATE,
        SEVERE
    }

    // getters and setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssessmentSession getSession() {
        return session;
    }

    public void setSession(AssessmentSession session) {
        this.session = session;
    }

    public Integer getCipnLabel() {
        return cipnLabel;
    }

    public void setCipnLabel(Integer cipnLabel) {
        this.cipnLabel = cipnLabel;
    }

    public CIPNSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(CIPNSeverity severity) {
        this.severity = severity;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }
}
