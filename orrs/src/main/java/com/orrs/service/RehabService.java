package com.orrs.service;

import com.orrs.model.*;
import com.orrs.repository.RehabPlanRepository;
import com.orrs.repository.RehabPlanWeekRepository;
import com.orrs.utils.RehabRuleEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RehabService {

    private final RehabPlanRepository rehabPlanRepository;
    private final RehabPlanWeekRepository rehabPlanWeekRepository;
    private final AiClientService aiClientService;

    public RehabPlan latestPlan(Long patientId) {
        return rehabPlanRepository.findTopByPatientIdOrderByPlanStartDateDesc(patientId);
    }

    public RehabPlan generateRehabPlan(AssessmentSession session,
                                       SymptomAssessment symptom,
                                       GaitAssessment gait,
                                       DexterityAssessment dex,
                                       boolean safeForHomeRehab) {

        int neuropathy = safe(symptom.getOverallNeuropathyScore());
        int pain = safe(symptom.getPainScore());
        int numb = safe(symptom.getNumbnessScore());

        int total = neuropathy + pain + numb;
        int cipnLabel = total >= 12 ? 1 : 0;

        RehabPlan plan = RehabRuleEngine.generatePlan(cipnLabel);

        plan.setPatient(session.getPatient());
        plan.setSession(session);
        plan.setPlanStartDate(LocalDate.now());
        plan.setNextReviewDate(LocalDate.now().plusWeeks(2));

        // If safety gate says "not safe", force clinician oversight.
        plan.setDoctorApprovalRequired(!safeForHomeRehab);
        plan.setApproved(safeForHomeRehab);

        // Enrich plan summary using patient profile (better personalization for cancer type/treatment).
        PatientProfile profile = session.getPatient();
        if (profile != null) {
            if (!profile.isProfileComplete()) {
                String extra = "NOTE: Patient profile is incomplete. Complete it for a more personalized rehab plan.";
                plan.setExercisePrescription(append(plan.getExercisePrescription(), extra));
            } else {
                String context = "Patient context: cancer=" + profile.getCancerType()
                        + ", stage=" + profile.getCancerStage()
                        + ", treatment=" + profile.getTreatmentType()
                        + ", comorbidities=" + profile.getComorbidities();
                plan.setExercisePrescription(append(plan.getExercisePrescription(), context));
            }
        }

        if (!safeForHomeRehab) {
            plan.setRehabLevel(RehabPlan.RehabLevel.BASIC);
            plan.setSafetyNotes("Doctor review required before starting home rehab. " +
                    (plan.getSafetyNotes() == null ? "" : plan.getSafetyNotes()));
        }
        RehabPlan saved = rehabPlanRepository.save(plan);

// Generate an AI-assisted 7-day structured plan (exercise + nutrition) and store JSON.
try {
    AiClientService.AiRehabWeeklyRequest req = new AiClientService.AiRehabWeeklyRequest();
// (profile already loaded above)
    if (profile != null) {
        req.setCancerType(profile.getCancerType());
        req.setCancerStage(profile.getCancerStage());
        req.setTreatmentType(profile.getTreatmentType());
        req.setDominantHand(profile.getDominantHand());
        req.setComorbidities(profile.getComorbidities());
    }
    int ctcae = (cipnLabel == 0) ? (total == 0 ? 0 : (total < 7 ? 1 : (total < 15 ? 2 : 3))) : (total < 20 ? 2 : 3);
    req.setCtcaeGrade(ctcae);
    req.setTugTimeSeconds(gait == null ? null : gait.getTugTimeSeconds());
    req.setCompletionTimeSeconds(dex == null || dex.getCompletionTimeSeconds() == null ? null : dex.getCompletionTimeSeconds().doubleValue());
    req.setErrorCount(dex == null ? 0 : dex.getErrorCount());
    req.setNeuropathyScore(neuropathy);
    req.setPainScore(pain);
    req.setNumbnessScore(numb);

    AiClientService.AiRehabWeeklyResponse week = aiClientService.generateWeeklyRehabPlan(req);
    if (week != null && week.getWeekPlan() != null) {
        RehabPlanWeek w = new RehabPlanWeek();
        w.setRehabPlan(saved);
        w.setSchemaVersion(week.getSchemaVersion());
        w.setWeekStart(LocalDate.parse(week.getWeekStart()));
        // store whole response JSON for frontend rendering later
        w.setWeekJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(week));
        rehabPlanWeekRepository.save(w);

        // Also append a readable summary into the existing text field for immediate UI use.
        String summary = buildWeekTextSummary(week);
        saved.setExercisePrescription(append(saved.getExercisePrescription(), summary));
        saved = rehabPlanRepository.save(saved);
    }
} catch (Exception ignored) {}

return saved;
    }

    private String append(String base, String extra) {
        if (base == null || base.isBlank()) return extra;
        return base + "\n\n" + extra;
    }

    private String buildWeekTextSummary(AiClientService.AiRehabWeeklyResponse week) {
        if (week == null || week.getWeekPlan() == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("WEEKLY REHAB SCHEDULE (7 days)\n");
        sb.append("Level: ").append(week.getRehabLevel()).append("\n");
        if (week.isDoctorApprovalRequired()) {
            sb.append("⚠ Doctor approval required before starting this week.\n");
        }
        if (week.getSafetyNotes() != null && !week.getSafetyNotes().isEmpty()) {
            sb.append("Safety notes: ");
            sb.append(String.join("; ", week.getSafetyNotes()));
            sb.append("\n");
        }
        sb.append("\n");

        int dayNum = 1;
        for (var dayObj : week.getWeekPlan()) {
            if (!(dayObj instanceof java.util.Map)) {
                dayNum++;
                continue;
            }
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> day = (java.util.Map<String, Object>) dayObj;

            Object d = day.get("day");
            Object title = day.get("title");
            Object rpe = day.get("rpeTarget");
            sb.append("Day ").append(d != null ? d : dayNum).append(": ")
              .append(title != null ? String.valueOf(title) : "Plan").append("\n");
            if (rpe != null) sb.append("  Target effort (RPE): ").append(rpe).append("\n");

            Object exercises = day.get("exercises");
            if (exercises instanceof java.util.List<?> exList && !exList.isEmpty()) {
                sb.append("  Exercises:\n");
                for (Object exObj : exList) {
                    if (!(exObj instanceof java.util.Map)) continue;
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> ex = (java.util.Map<String, Object>) exObj;
                    sb.append("   • ")
                      .append(val(ex, "category")).append(": ")
                      .append(val(ex, "name"));
                    String dose = buildDose(ex);
                    if (!dose.isBlank()) sb.append(" (").append(dose).append(")");
                    sb.append("\n");
                }
            }

            Object nutrition = day.get("nutrition");
            if (nutrition instanceof java.util.List<?> nList && !nList.isEmpty()) {
                sb.append("  Nutrition:\n");
                for (Object nObj : nList) {
                    if (!(nObj instanceof java.util.Map)) continue;
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> n = (java.util.Map<String, Object>) nObj;
                    sb.append("   • ").append(val(n, "goal")).append(": ").append(val(n, "text"));
                    Object sug = n.get("suggestion");
                    if (sug != null && !String.valueOf(sug).isBlank()) {
                        sb.append(" — ").append(String.valueOf(sug));
                    }
                    sb.append("\n");
                }
            }

            sb.append("\n");
            dayNum++;
        }
        return sb.toString().trim();
    }

    private String val(java.util.Map<String, Object> m, String k) {
        Object v = m.get(k);
        return v == null ? "-" : String.valueOf(v);
    }

    private String buildDose(java.util.Map<String, Object> ex) {
        Object dur = ex.get("durationMin");
        Object sets = ex.get("sets");
        Object reps = ex.get("reps");
        StringBuilder d = new StringBuilder();
        if (dur != null) d.append(dur).append(" min");
        if (sets != null) {
            if (d.length() > 0) d.append(", ");
            d.append(sets).append(" sets");
        }
        if (reps != null) {
            if (d.length() > 0) d.append(", ");
            d.append(reps).append(" reps");
        }
        return d.toString();
    }

    private int safe(Integer v) { return v == null ? 0 : v; }
}