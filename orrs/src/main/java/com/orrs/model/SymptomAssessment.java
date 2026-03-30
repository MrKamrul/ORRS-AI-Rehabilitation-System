package com.orrs.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "symptom_assessments")
public class SymptomAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private AssessmentSession session;

    private Integer numbnessScore;   // 0–10
    private Integer tinglingScore;   // 0–10
    private Integer painScore;       // 0–10
    private Integer burningScore;    // 0–10

    private Integer overallNeuropathyScore;

    // getters & setters


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

    public Integer getNumbnessScore() {
        return numbnessScore;
    }

    public void setNumbnessScore(Integer numbnessScore) {
        this.numbnessScore = numbnessScore;
    }

    public Integer getTinglingScore() {
        return tinglingScore;
    }

    public void setTinglingScore(Integer tinglingScore) {
        this.tinglingScore = tinglingScore;
    }

    public Integer getPainScore() {
        return painScore;
    }

    public void setPainScore(Integer painScore) {
        this.painScore = painScore;
    }

    public Integer getBurningScore() {
        return burningScore;
    }

    public void setBurningScore(Integer burningScore) {
        this.burningScore = burningScore;
    }

    public Integer getOverallNeuropathyScore() {
        return overallNeuropathyScore;
    }

    public void setOverallNeuropathyScore(Integer overallNeuropathyScore) {
        this.overallNeuropathyScore = overallNeuropathyScore;
    }
}
