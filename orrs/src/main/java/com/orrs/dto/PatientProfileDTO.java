package com.orrs.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Patient profile fields used for rehab planning and CIPN contextualization.
 * All fields are required for "profile complete".
 */
public class PatientProfileDTO {

    @NotNull @Min(1)
    private Integer age;

    @NotBlank
    private String gender;

    @NotNull @DecimalMin(value = "1.0", inclusive = false)
    private Double heightCm;

    @NotBlank
    private String dominantHand;

    @NotBlank
    private String cancerType;

    @NotBlank
    private String cancerStage;

    @NotBlank
    private String primarySite;

    @NotBlank
    private String treatmentType;

    @NotBlank
    private String chemoAgents;

    @NotBlank
    private String radiationSite;

    @NotNull
    private Boolean surgeryPerformed;

    @NotNull
    private LocalDate treatmentStartDate;

    @NotNull
    private LocalDate treatmentEndDate;

    @NotNull @DecimalMin(value = "1.0", inclusive = false)
    private Double weightBeforeTreatment;

    @NotNull @DecimalMin(value = "1.0", inclusive = false)
    private Double weightAfterTreatment;

    @NotBlank
    private String comorbidities;

    @NotBlank
    private String currentPainAreas;

    @NotBlank
    private String activityLevel;

    @NotNull
    private Boolean baselineNeuropathy;

    // Getters & setters

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
