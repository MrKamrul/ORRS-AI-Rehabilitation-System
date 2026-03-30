package com.orrs.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.orrs.model.*;
import com.orrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AssessmentSessionRepository sessionRepo;
    private final SymptomAssessmentRepository symptomRepo;
    private final GaitAssessmentRepository gaitRepo;
    private final DexterityAssessmentRepository dexRepo;
    private final CIPNPredictionRepository predictionRepo;
    private final RehabPlanRepository rehabRepo;
    private final DoctorNoteRepository noteRepo;

    public byte[] buildSessionReportPdf(Long sessionId) {
        AssessmentSession session = sessionRepo.findById(sessionId).orElseThrow();
        SymptomAssessment s = symptomRepo.findBySessionId(sessionId);
        GaitAssessment g = gaitRepo.findBySessionId(sessionId);
        DexterityAssessment d = dexRepo.findBySessionId(sessionId);
        CIPNPrediction p = predictionRepo.findBySessionId(sessionId);
        RehabPlan plan = rehabRepo.findAll().stream()
                .filter(r -> r.getSession() != null && sessionId.equals(r.getSession().getId()))
                .findFirst().orElse(null);

        var notes = noteRepo.findByPatientIdAndSessionIdOrderByCreatedAtDesc(session.getPatient().getId(), sessionId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        Font h1 = new Font(Font.HELVETICA, 16, Font.BOLD);
        Font h2 = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font body = new Font(Font.HELVETICA, 11, Font.NORMAL);

        doc.add(new Paragraph("NeuroTrack+ Clinical Progress Report", h1));
        doc.add(new Paragraph(" "));

        PatientProfile patient = session.getPatient();
        String patientName = patient.getUser() != null ? patient.getUser().getFullName() : "(Unknown)";
        doc.add(new Paragraph("Patient: " + patientName, body));
        doc.add(new Paragraph("Session ID: " + session.getId(), body));
        doc.add(new Paragraph("Assessment Date: " + session.getAssessmentDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), body));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("1) Symptoms (CIPN)", h2));
        PdfPTable t1 = new PdfPTable(2);
        t1.setWidthPercentage(100);
        addRow(t1, "Overall Neuropathy (0-10)", s == null ? "-" : String.valueOf(s.getOverallNeuropathyScore()));
        addRow(t1, "Pain (0-10)", s == null ? "-" : String.valueOf(s.getPainScore()));
        addRow(t1, "Numbness (0-10)", s == null ? "-" : String.valueOf(s.getNumbnessScore()));
        doc.add(t1);
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("2) Physical Tests", h2));
        PdfPTable t2 = new PdfPTable(2);
        t2.setWidthPercentage(100);
        addRow(t2, "Timed Up & Go (seconds)", g == null ? "-" : String.valueOf(g.getTugTimeSeconds()));
        addRow(t2, "Gait Risk", g == null ? "-" : String.valueOf(g.getGaitRiskLevel()));
        addRow(t2, "Dexterity Time (seconds)", d == null ? "-" : String.valueOf(d.getCompletionTimeSeconds()));
        addRow(t2, "Dexterity Errors", d == null ? "-" : String.valueOf(d.getErrorCount()));
        addRow(t2, "Dexterity Score", d == null ? "-" : String.valueOf(d.getDexterityScore()));
        doc.add(t2);
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("3) Prediction", h2));
        if (p == null) {
            doc.add(new Paragraph("No prediction stored for this session.", body));
        } else {
            doc.add(new Paragraph("Severity: " + p.getSeverity(), body));
            doc.add(new Paragraph("Confidence: " + (p.getConfidenceScore() == null ? "-" : p.getConfidenceScore()), body));
            if (p.getExplanation() != null && !p.getExplanation().isBlank()) {
                doc.add(new Paragraph("Explanation: " + p.getExplanation(), body));
            }
        }
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("4) Rehabilitation Plan", h2));
        if (plan == null) {
            doc.add(new Paragraph("No rehab plan stored for this session.", body));
        } else {
            doc.add(new Paragraph("Level: " + plan.getRehabLevel(), body));
            doc.add(new Paragraph("Plan: " + safe(plan.getExercisePrescription()), body));
            if (plan.getSafetyNotes() != null && !plan.getSafetyNotes().isBlank()) {
                doc.add(new Paragraph("Safety notes: " + plan.getSafetyNotes(), body));
            }
            doc.add(new Paragraph("Doctor approval required: " + plan.isDoctorApprovalRequired(), body));
            doc.add(new Paragraph("Approved: " + plan.isApproved(), body));
        }
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("5) Doctor Notes", h2));
        if (notes.isEmpty()) {
            doc.add(new Paragraph("No doctor notes for this session.", body));
        } else {
            for (DoctorNote n : notes) {
                String dr = n.getDoctor() != null ? n.getDoctor().getFullName() : "Doctor";
                doc.add(new Paragraph("- " + n.getCreatedAt().toString() + " (" + dr + "): " + n.getNote(), body));
            }
        }

        doc.close();
        return baos.toByteArray();
    }

    private static void addRow(PdfPTable t, String k, String v) {
        t.addCell(new Phrase(k));
        t.addCell(new Phrase(v));
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
