package com.orrs.service;

import com.orrs.model.*;
import com.orrs.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final GaitAssessmentRepository gaitRepo;
    private final DexterityAssessmentRepository dexRepo;
    private final SymptomAssessmentRepository symptomRepo;
    private final AssessmentSessionRepository sessionRepo;
    private final PatientProfileRepository patientProfileRepository;

    public AssessmentSession saveAssessmentForPatientUser(User user, GaitAssessment gait, DexterityAssessment dexterity, SymptomAssessment symptom) {

        PatientProfile patient = patientProfileRepository.findByUserId(user.getId());
        if (patient == null) {
            patient = new PatientProfile();
            patient.setUser(user);
            patient = patientProfileRepository.save(patient);
        }

        AssessmentSession session = new AssessmentSession();
        session.setPatient(patient);
        session.setAssessmentDate(LocalDateTime.now());
        session.setAssessedBy("PATIENT_SELF");
        session = sessionRepo.save(session);

        gait.setSession(session);
        dexterity.setSession(session);
        symptom.setSession(session);

        gaitRepo.save(gait);
        dexRepo.save(dexterity);
        symptomRepo.save(symptom);

        return session;
    }

    public java.util.List<AssessmentSession> findSessionsForPatient(Long patientId) {
        return sessionRepo.findByPatientIdOrderByAssessmentDateDesc(patientId);
    }

    public AssessmentSession findLatestSession(Long patientId) {
        return sessionRepo.findTopByPatientIdOrderByAssessmentDateDesc(patientId);
    }
}
