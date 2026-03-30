package com.orrs.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Patient rehabilitation profile.
 *
 * NOTE: We create an empty profile at registration time. Because of that, we do NOT
 * enforce DB-level NOT NULL. Instead we enforce "required" at the API/UI layer, and
 * provide a completion indicator for the dashboard.
 */
@Entity
@Table(name = "patient_profiles")
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // Demographics
    private Integer age;
    private String gender; // MALE / FEMALE / OTHER
    private Double heightCm;
    private String dominantHand; // RIGHT / LEFT

    // Cancer & treatment
    private String cancerType;       // e.g., Breast, Lung, Colorectal
    private String cancerStage;      // I / II / III / IV (or text)
    private String primarySite;      // e.g., Left breast, Colon
    private String treatmentType;    // CHEMO / RADIO / BOTH / SURGERY / COMBINED
    private String chemoAgents;      // comma-separated (MVP)
    private String radiationSite;    // e.g., Chest wall
    private Boolean surgeryPerformed;

    private LocalDate treatmentStartDate;
    private LocalDate treatmentEndDate;

    // Body changes
    private Double weightBeforeTreatment;
    private Double weightAfterTreatment;

    // Rehab-relevant factors
    private String comorbidities;      // diabetes, neuropathy, etc.
    private String currentPainAreas;   // e.g., shoulder, back
    private String activityLevel;      // LOW / MODERATE / HIGH
    private Boolean baselineNeuropathy;

    // --------------------
    // Completion utilities
    // --------------------

    /**
     * Returns a list of missing required fields (for UI red-dot / checklist).
     */
    public List<String> missingRequiredFields() {
        List<String> missing = new ArrayList<>();

        if (age == null || age <= 0) missing.add("age");
        if (isBlank(gender)) missing.add("gender");
        if (heightCm == null || heightCm <= 0) missing.add("heightCm");
        if (isBlank(dominantHand)) missing.add("dominantHand");

        if (isBlank(cancerType)) missing.add("cancerType");
        if (isBlank(cancerStage)) missing.add("cancerStage");
        if (isBlank(primarySite)) missing.add("primarySite");
        if (isBlank(treatmentType)) missing.add("treatmentType");
        if (isBlank(chemoAgents)) missing.add("chemoAgents");
        if (isBlank(radiationSite)) missing.add("radiationSite");
        if (surgeryPerformed == null) missing.add("surgeryPerformed");

        if (treatmentStartDate == null) missing.add("treatmentStartDate");
        if (treatmentEndDate == null) missing.add("treatmentEndDate");

        if (weightBeforeTreatment == null || weightBeforeTreatment <= 0) missing.add("weightBeforeTreatment");
        if (weightAfterTreatment == null || weightAfterTreatment <= 0) missing.add("weightAfterTreatment");

        if (isBlank(comorbidities)) missing.add("comorbidities");
        if (isBlank(currentPainAreas)) missing.add("currentPainAreas");
        if (isBlank(activityLevel)) missing.add("activityLevel");
        if (baselineNeuropathy == null) missing.add("baselineNeuropathy");

        return missing;
    }

    public boolean isProfileComplete() {
        return missingRequiredFields().isEmpty();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    // getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public String getDominantHand() { return dominantHand; }
    public void setDominantHand(String dominantHand) { this.dominantHand = dominantHand; }

    public String getCancerType() { return cancerType; }
    public void setCancerType(String cancerType) { this.cancerType = cancerType; }

    public String getCancerStage() { return cancerStage; }
    public void setCancerStage(String cancerStage) { this.cancerStage = cancerStage; }

    public String getPrimarySite() { return primarySite; }
    public void setPrimarySite(String primarySite) { this.primarySite = primarySite; }

    public String getTreatmentType() { return treatmentType; }
    public void setTreatmentType(String treatmentType) { this.treatmentType = treatmentType; }

    public String getChemoAgents() { return chemoAgents; }
    public void setChemoAgents(String chemoAgents) { this.chemoAgents = chemoAgents; }

    public String getRadiationSite() { return radiationSite; }
    public void setRadiationSite(String radiationSite) { this.radiationSite = radiationSite; }

    public Boolean getSurgeryPerformed() { return surgeryPerformed; }
    public void setSurgeryPerformed(Boolean surgeryPerformed) { this.surgeryPerformed = surgeryPerformed; }

    public LocalDate getTreatmentStartDate() { return treatmentStartDate; }
    public void setTreatmentStartDate(LocalDate treatmentStartDate) { this.treatmentStartDate = treatmentStartDate; }

    public LocalDate getTreatmentEndDate() { return treatmentEndDate; }
    public void setTreatmentEndDate(LocalDate treatmentEndDate) { this.treatmentEndDate = treatmentEndDate; }

    public Double getWeightBeforeTreatment() { return weightBeforeTreatment; }
    public void setWeightBeforeTreatment(Double weightBeforeTreatment) { this.weightBeforeTreatment = weightBeforeTreatment; }

    public Double getWeightAfterTreatment() { return weightAfterTreatment; }
    public void setWeightAfterTreatment(Double weightAfterTreatment) { this.weightAfterTreatment = weightAfterTreatment; }

    public String getComorbidities() { return comorbidities; }
    public void setComorbidities(String comorbidities) { this.comorbidities = comorbidities; }

    public String getCurrentPainAreas() { return currentPainAreas; }
    public void setCurrentPainAreas(String currentPainAreas) { this.currentPainAreas = currentPainAreas; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public Boolean getBaselineNeuropathy() { return baselineNeuropathy; }
    public void setBaselineNeuropathy(Boolean baselineNeuropathy) { this.baselineNeuropathy = baselineNeuropathy; }
}
