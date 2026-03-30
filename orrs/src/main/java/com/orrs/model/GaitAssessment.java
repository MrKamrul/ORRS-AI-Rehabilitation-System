package com.orrs.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "gait_assessments")
public class GaitAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private AssessmentSession session;

    // Timed Up & Go (seconds)
    private Double tugTimeSeconds;

    // Derived from rule-based logic
    @Enumerated(EnumType.STRING)
    private GaitRiskLevel gaitRiskLevel;

    // Optional clinical observation
    private Boolean balanceIssueObserved;

    public enum GaitRiskLevel {
        LOW,
        MODERATE,
        HIGH
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

    public Double getTugTimeSeconds() {
        return tugTimeSeconds;
    }

    public void setTugTimeSeconds(Double tugTimeSeconds) {
        this.tugTimeSeconds = tugTimeSeconds;
    }

    public GaitRiskLevel getGaitRiskLevel() {
        return gaitRiskLevel;
    }

    public void setGaitRiskLevel(GaitRiskLevel gaitRiskLevel) {
        this.gaitRiskLevel = gaitRiskLevel;
    }

    public Boolean getBalanceIssueObserved() {
        return balanceIssueObserved;
    }

    public void setBalanceIssueObserved(Boolean balanceIssueObserved) {
        this.balanceIssueObserved = balanceIssueObserved;
    }
}