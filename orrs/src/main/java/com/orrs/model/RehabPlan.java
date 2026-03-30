package com.orrs.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rehab_plans")
public class RehabPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private PatientProfile patient;

    /** The assessment session that triggered this plan (plan versioning / timeline). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    @JsonIgnore
    private AssessmentSession session;

    @Enumerated(EnumType.STRING)
    private RehabLevel rehabLevel;

    @Column(length = 4000)
    private String exercisePrescription;

    @Column(length = 2000)
    private String safetyNotes;

    private LocalDate planStartDate;
    private LocalDate nextReviewDate;

    // Doctor workflow
    private boolean doctorApprovalRequired;
    private boolean approved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    private LocalDateTime approvedAt;

    public enum RehabLevel {
        BASIC,
        INTERMEDIATE,
        ADVANCED
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PatientProfile getPatient() {
        return patient;
    }

    public void setPatient(PatientProfile patient) {
        this.patient = patient;
    }

    public AssessmentSession getSession() {
        return session;
    }

    public void setSession(AssessmentSession session) {
        this.session = session;
    }

    public RehabLevel getRehabLevel() {
        return rehabLevel;
    }

    public void setRehabLevel(RehabLevel rehabLevel) {
        this.rehabLevel = rehabLevel;
    }

    public String getExercisePrescription() {
        return exercisePrescription;
    }

    public void setExercisePrescription(String exercisePrescription) {
        this.exercisePrescription = exercisePrescription;
    }

    public String getSafetyNotes() {
        return safetyNotes;
    }

    public void setSafetyNotes(String safetyNotes) {
        this.safetyNotes = safetyNotes;
    }

    public LocalDate getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(LocalDate planStartDate) {
        this.planStartDate = planStartDate;
    }

    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public boolean isDoctorApprovalRequired() {
        return doctorApprovalRequired;
    }

    public void setDoctorApprovalRequired(boolean doctorApprovalRequired) {
        this.doctorApprovalRequired = doctorApprovalRequired;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
