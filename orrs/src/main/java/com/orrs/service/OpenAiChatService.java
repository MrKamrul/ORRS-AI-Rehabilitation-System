package com.orrs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAiChatService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.openai.baseUrl:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${app.openai.model:gpt-5.2-mini}")
    private String model;

    public String reply(String apiKey, List<Map<String, String>> history, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Chatbot is not configured. Set OPENAI_API_KEY environment variable on the server.";
        }

        // Build Responses API payload
        // We keep prompts supportive and non-diagnostic.
        List<Object> input = new ArrayList<>();
        input.add(roleMsg("system",
                "You are NeuroTrack+, a supportive emotional companion for a cancer survivor in rehabilitation. " +
                "Be empathetic, practical, and motivating. Do not provide medical diagnosis. " +
                "If user mentions severe symptoms, advise contacting their clinician. " +
                "If user mentions self-harm, encourage immediate local emergency help."
        ));

        // add a short history (last 10 turns)
        if (history != null) {
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> m = history.get(i);
                input.add(roleMsg(m.getOrDefault("role","user"), m.getOrDefault("content","")));
            }
        }
        input.add(roleMsg("user", userMessage));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("input", input);

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");
            org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);

            String resp = restTemplate.postForObject(baseUrl + "/responses", entity, String.class);
            if (resp == null) return "Sorry, I couldn't generate a response right now.";

            JsonNode root = mapper.readTree(resp);

            // Try common fields:
            // - output_text (if present)
            JsonNode outputText = root.get("output_text");
            if (outputText != null && outputText.isTextual()) return outputText.asText();

            // Otherwise parse from output array
            JsonNode outArr = root.get("output");
            if (outArr != null && outArr.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode o : outArr) {
                    JsonNode content = o.get("content");
                    if (content != null && content.isArray()) {
                        for (JsonNode c : content) {
                            if ("output_text".equals(c.path("type").asText()) && c.has("text")) {
                                sb.append(c.get("text").asText());
                            }
                        }
                    }
                }
                String text = sb.toString().trim();
                if (!text.isEmpty()) return text;
            }

            return "Sorry, I couldn't parse the response.";
        } catch (RestClientException | java.io.IOException e) {
            return "Chatbot error: " + e.getMessage();
        }
    }

    private Map<String, Object> roleMsg(String role, String text) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("role", role);
        msg.put("content", List.of(Map.of("type", "text", "text", text)));
        return msg;
    }
}
