package com.orrs.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "dexterity_assessments")
public class DexterityAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private AssessmentSession session;

    // Time to complete task (seconds)
    private Integer completionTimeSeconds;

    // Missed taps / wrong sequence
    private Integer errorCount;

    // Normalized score (0–100)
    private Integer dexterityScore;

    // LEFT / RIGHT / BOTH
    private String handUsed;

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

    public Integer getCompletionTimeSeconds() {
        return completionTimeSeconds;
    }

    public void setCompletionTimeSeconds(Integer completionTimeSeconds) {
        this.completionTimeSeconds = completionTimeSeconds;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getDexterityScore() {
        return dexterityScore;
    }

    public void setDexterityScore(Integer dexterityScore) {
        this.dexterityScore = dexterityScore;
    }

    public String getHandUsed() {
        return handUsed;
    }

    public void setHandUsed(String handUsed) {
        this.handUsed = handUsed;
    }
}
