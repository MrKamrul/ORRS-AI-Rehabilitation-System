package com.orrs.repository;

import com.orrs.model.DoctorNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {
    List<DoctorNote> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<DoctorNote> findByPatientIdAndSessionIdOrderByCreatedAtDesc(Long patientId, Long sessionId);
}
