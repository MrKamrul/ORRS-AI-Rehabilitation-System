package com.orrs.controller;

import com.orrs.model.AssessmentSession;
import com.orrs.model.DoctorNote;
import com.orrs.model.RehabPlan;
import com.orrs.model.PatientProfile;
import com.orrs.repository.AssessmentSessionRepository;
import com.orrs.repository.DoctorNoteRepository;
import com.orrs.repository.PatientProfileRepository;
import com.orrs.repository.RehabPlanRepository;
import com.orrs.repository.UserRepository;
import com.orrs.service.ReportService;
import com.orrs.service.SessionDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final PatientProfileRepository patientRepo;
    private final AssessmentSessionRepository sessionRepo;
    private final DoctorNoteRepository noteRepo;
    private final RehabPlanRepository rehabPlanRepo;
    private final UserRepository userRepo;
    private final ReportService reportService;
    private final SessionDetailService sessionDetailService;

    @GetMapping("/patients")
    public List<PatientProfile> listPatients() {
        return patientRepo.findAll();
    }

    @GetMapping("/patients/{patientId}/sessions")
    public List<AssessmentSession> patientSessions(@PathVariable Long patientId) {
        return sessionRepo.findByPatientIdOrderByAssessmentDateDesc(patientId);
    }

    @GetMapping("/patients/{patientId}/sessions/{sessionId}")
    public java.util.Map<String, Object> sessionDetails(@PathVariable Long patientId, @PathVariable Long sessionId) {
        var details = sessionDetailService.details(sessionId);
        AssessmentSession session = (AssessmentSession) details.get("session");
        if (session.getPatient() == null || session.getPatient().getId() == null || !session.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Session does not belong to patient");
        }
        return details;
    }

    @GetMapping("/patients/{patientId}/notes")
    public List<DoctorNote> notes(@PathVariable Long patientId) {
        return noteRepo.findByPatientIdOrderByCreatedAtDesc(patientId);
    }

    @PostMapping("/patients/{patientId}/notes")
    public DoctorNote addNote(Authentication auth, @PathVariable Long patientId, @RequestBody java.util.Map<String, Object> body) {
        String noteText = String.valueOf(body.getOrDefault("note", "")).trim();
        Long sessionId = body.get("sessionId") == null ? null : Long.valueOf(String.valueOf(body.get("sessionId")));
        if (noteText.isEmpty()) throw new IllegalArgumentException("Note cannot be empty");

        var doctor = userRepo.findByEmail(auth.getName());
        var patient = patientRepo.findById(patientId).orElseThrow();

        DoctorNote n = new DoctorNote();
        n.setDoctor(doctor);
        n.setPatient(patient);
        if (sessionId != null) {
            sessionRepo.findById(sessionId).ifPresent(n::setSession);
        }
        n.setNote(noteText);
        return noteRepo.save(n);
    }

    @PutMapping("/rehab-plans/{planId}")
    public RehabPlan updatePlan(Authentication auth, @PathVariable Long planId, @RequestBody java.util.Map<String, Object> body) {
        var doctor = userRepo.findByEmail(auth.getName());
        RehabPlan plan = rehabPlanRepo.findById(planId).orElseThrow();

        if (body.containsKey("exercisePrescription")) {
            plan.setExercisePrescription(String.valueOf(body.get("exercisePrescription")));
        }
        if (body.containsKey("safetyNotes")) {
            plan.setSafetyNotes(String.valueOf(body.get("safetyNotes")));
        }
        // Approve action
        if (body.containsKey("approved")) {
            boolean approved = Boolean.parseBoolean(String.valueOf(body.get("approved")));
            plan.setApproved(approved);
            if (approved) {
                plan.setApprovedBy(doctor);
                plan.setApprovedAt(java.time.LocalDateTime.now());
            }
        }
        return rehabPlanRepo.save(plan);
    }

    @GetMapping(value = "/patients/{patientId}/report/pdf", produces = "application/pdf")
    public org.springframework.http.ResponseEntity<byte[]> downloadPatientReport(@PathVariable Long patientId,
                                                                               @RequestParam Long sessionId) {
        // In a real system, ...
        byte[] pdf = reportService.buildSessionReportPdf(sessionId);
        String filename = "NeuroTrack_Patient_" + patientId + "_Session_" + sessionId + ".pdf";
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
