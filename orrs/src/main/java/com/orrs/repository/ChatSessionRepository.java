package com.orrs.repository;

import com.orrs.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    ChatSession findTopByPatientIdOrderByIdDesc(Long patientId);
}
