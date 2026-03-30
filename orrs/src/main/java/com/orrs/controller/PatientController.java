package com.orrs.controller;

import com.orrs.dto.AssessmentDTO;
import com.orrs.model.*;
import com.orrs.repository.UserRepository;
import com.orrs.repository.NotificationRepository;
import com.orrs.service.AssessmentService;
import com.orrs.service.ClinicalTriageService;
import com.orrs.service.CIPNPredictionService;
import com.orrs.service.RehabService;
import com.orrs.service.ReportService;
import com.orrs.service.SessionDetailService;
import com.orrs.service.PatientProfileService;
import com.orrs.utils.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final UserRepository userRepository;
    private final AssessmentService assessmentService;
    private final ClinicalTriageService triageService;
    private final CIPNPredictionService predictionService;
    private final RehabService rehabService;
    private final NotificationRepository notificationRepository;
    private final ReportService reportService;
    private final SessionDetailService sessionDetailService;
    private final PatientProfileService patientProfileService;

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName());
        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getId());
        res.put("email", user.getEmail());
        res.put("fullName", user.getFullName());
        res.put("role", user.getRole());
        return res;
    }

    @PostMapping("/assessment")
    public Map<String, Object> submitAssessment(Authentication auth, @RequestBody AssessmentDTO dto) {
        User user = userRepository.findByEmail(auth.getName());

        // Build assessments (scores are calculated here)
        GaitAssessment gait = new GaitAssessment();
        gait.setTugTimeSeconds(dto.getTugTimeSeconds());
        gait.setGaitRiskLevel(ScoreCalculator.calculateGaitRisk(dto.getTugTimeSeconds()));

        DexterityAssessment dex = new DexterityAssessment();
        dex.setCompletionTimeSeconds(dto.getCompletionTimeSeconds());
        dex.setErrorCount(dto.getErrorCount());
        dex.setDexterityScore(ScoreCalculator.calculateDexterityScore(dto.getCompletionTimeSeconds(), dto.getErrorCount()));
        dex.setHandUsed(dto.getHandUsed());

        SymptomAssessment symptom = new SymptomAssessment();
        symptom.setOverallNeuropathyScore(dto.getNeuropathyScore());
        symptom.setPainScore(dto.getPainScore());
        symptom.setNumbnessScore(dto.getNumbnessScore());

        var session = assessmentService.saveAssessmentForPatientUser(user, gait, dex, symptom);

        // Safety gate
        Map<String, Object> triage = triageService.triage(gait, dex, symptom);

        // Prediction + rehab plan
        CIPNPrediction prediction = predictionService.generatePrediction(session, symptom);
        RehabPlan plan = rehabService.generateRehabPlan(session, symptom, gait, dex, (boolean) triage.get("safeForHomeRehab"));

        // Notifications (patient-facing)
        boolean needsReview = Boolean.TRUE.equals(triage.get("requiresDoctorReview"));
        if (needsReview) {
            Notification n = new Notification();
            n.setUser(user);
            n.setType("TRIAGE");
            n.setMessage("Safety check: a clinician review is required before starting the rehab plan.");
            notificationRepository.save(n);
        }
        if (prediction != null && prediction.getSeverity() == CIPNPrediction.CIPNSeverity.SEVERE) {
            Notification n = new Notification();
            n.setUser(user);
            n.setType("ALERT");
            n.setMessage("Your CIPN risk was predicted as SEVERE. Please contact your doctor for review.");
            notificationRepository.save(n);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("sessionId", session.getId());
        res.put("triage", triage);
        res.put("prediction", prediction);
        res.put("rehabPlan", plan);
        return res;
    }

    @GetMapping("/sessions")
    public java.util.List<AssessmentSession> mySessions(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName());
        PatientProfile profile = patientProfileService.getOrCreate(user);
        return assessmentService.findSessionsForPatient(profile.getId());
    }

    @GetMapping("/sessions/{sessionId}")
    public Map<String, Object> sessionDetails(Authentication auth, @PathVariable Long sessionId) {
        User user = userRepository.findByEmail(auth.getName());
        PatientProfile profile = patientProfileService.getOrCreate(user);

        Map<String, Object> details = sessionDetailService.details(sessionId);
        AssessmentSession session = (AssessmentSession) details.get("session");
        if (session.getPatient() == null || !session.getPatient().getId().equals(profile.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Not your session");
        }
        return details;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName());
        Map<String, Object> res = new HashMap<>();
        res.put("userId", user.getId());
        res.put("fullName", user.getFullName());
        res.put("email", user.getEmail());

        // Ensure a profile exists for patients; UI uses completion for red-dot.
        PatientProfile profile = patientProfileService.getOrCreate(user);
        res.put("profileComplete", profile.isProfileComplete());
        res.put("missingProfileFields", profile.missingRequiredFields());
        // If profile is not linked in user entity yet, use repository lookup.
        Long pid = profile.getId();
        var last = assessmentService.findLatestSession(pid);
        res.put("lastAssessmentDate", last == null ? null : last.getAssessmentDate());
        res.put("latestRehabPlan", rehabService.latestPlan(pid));
        res.put("unreadNotifications", notificationRepository.countByUserIdAndReadFlagFalse(user.getId()));
        return res;
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName());
        PatientProfile p = patientProfileService.getOrCreate(user);

        Map<String, Object> res = new HashMap<>();
        res.put("fullName", user.getFullName());
        res.put("email", user.getEmail());
        res.put("profile", p);
        res.put("profileComplete", p.isProfileComplete());
        res.put("missingProfileFields", p.missingRequiredFields());
        return res;
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(Authentication auth, @Valid @RequestBody com.orrs.dto.PatientProfileDTO dto) {
        User user = userRepository.findByEmail(auth.getName());
        PatientProfile updated = patientProfileService.update(user, dto);

        Map<String, Object> res = new HashMap<>();
        res.put("profile", updated);
        res.put("profileComplete", updated.isProfileComplete());
        res.put("missingProfileFields", updated.missingRequiredFields());
        return res;
    }

    @GetMapping("/notifications")
    public java.util.List<Notification> notifications(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName());
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @PutMapping("/notifications/{id}/read")
    public void markRead(Authentication auth, @PathVariable Long id) {
        User user = userRepository.findByEmail(auth.getName());
        var nOpt = notificationRepository.findById(id);
        if (nOpt.isPresent() && nOpt.get().getUser().getId().equals(user.getId())) {
            Notification n = nOpt.get();
            n.setReadFlag(true);
            notificationRepository.save(n);
        }
    }

    @GetMapping(value = "/report/pdf", produces = "application/pdf")
    public org.springframework.http.ResponseEntity<byte[]> downloadReport(Authentication auth,
                                                                          @RequestParam(required = false) Long sessionId) {
        User user = userRepository.findByEmail(auth.getName());
        Long pid = patientProfileService.getOrCreate(user).getId();
        if (sessionId == null) {
            var latest = assessmentService.findLatestSession(pid);
            if (latest == null) return org.springframework.http.ResponseEntity.notFound().build();
            sessionId = latest.getId();
        }
        byte[] pdf = reportService.buildSessionReportPdf(sessionId);
        String filename = "NeuroTrack_Report_Session_" + sessionId + ".pdf";
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
