package com.orrs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ChatSession chatSession;

    private String sender; // PATIENT / ASSISTANT

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
}
