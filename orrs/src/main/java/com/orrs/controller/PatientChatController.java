package com.orrs.controller;

import com.orrs.model.ChatMessage;
import com.orrs.model.ChatSession;
import com.orrs.model.PatientProfile;
import com.orrs.model.User;
import com.orrs.repository.ChatMessageRepository;
import com.orrs.repository.ChatSessionRepository;
import com.orrs.repository.UserRepository;
import com.orrs.service.OpenAiChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/patient/chat")
@RequiredArgsConstructor
public class PatientChatController {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final OpenAiChatService openAiChatService;

    @PostMapping
    public Map<String, Object> chat(Authentication auth, @RequestBody ChatRequest req) {
        User user = userRepository.findByEmail(auth.getName());
        PatientProfile patient = user.getPatientProfile();
        if (patient == null) throw new IllegalArgumentException("Patient profile not found");

        ChatSession session = chatSessionRepository.findTopByPatientIdOrderByIdDesc(patient.getId());
        if (session == null) {
            session = new ChatSession();
            session.setPatient(patient);
            session = chatSessionRepository.save(session);
        }

        // store patient message
        ChatMessage m1 = new ChatMessage();
        m1.setChatSession(session);
        m1.setSender("PATIENT");
        m1.setMessage(req.getMessage());
        chatMessageRepository.save(m1);

        // build short history (oldest->newest)
        List<ChatMessage> last = chatMessageRepository.findTop20ByChatSessionIdOrderByIdDesc(session.getId());
        Collections.reverse(last);

        List<Map<String, String>> history = new ArrayList<>();
        for (ChatMessage m : last) {
            String role = "PATIENT".equals(m.getSender()) ? "user" : "assistant";
            history.add(Map.of("role", role, "content", m.getMessage()));
        }

        String apiKey = System.getenv("OPENAI_API_KEY");
        String reply = openAiChatService.reply(apiKey, history, req.getMessage());

        ChatMessage m2 = new ChatMessage();
        m2.setChatSession(session);
        m2.setSender("ASSISTANT");
        m2.setMessage(reply);
        chatMessageRepository.save(m2);

        return Map.of(
                "chatSessionId", session.getId(),
                "reply", reply
        );
    }

    @Data
    public static class ChatRequest {
        private String message;
    }
}
