package com.rishanth.deskmind.service;

import com.rishanth.deskmind.dto.AiClassificationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AIService {

    private final ChatClient chatClient;

    public AIService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AiClassificationResult classifyTicket(String title, String description) {
        String promptText = String.format("""
            You are an expert IT Helpdesk Triage Agent.
            Classify the following support ticket based on its title and description.
            
            Title: %s
            Description: %s
            
            Respond ONLY with a valid JSON object in this exact format.
            No explanation, no markdown, no code blocks, just raw JSON:
            {
                "category": "TECHNICAL or BILLING or GENERAL",
                "priority": "P1 or P2 or P3 or P4",
                "confidence": <number between 0 and 100>,
                "reply": "A helpful response message to the customer"
            }
            
            Priority guide:
            P1 = Critical, production is down
            P2 = High impact but workaround exists
            P3 = Medium impact
            P4 = Low impact or general inquiry
            """, title, description);

        try {
            log.info("Sending ticket to AI for classification...");
            return chatClient.prompt()
                    .user(promptText)
                    .call()
                    .entity(AiClassificationResult.class);

        } catch (Exception e) {
            log.warn("AI Error: {}. Falling back to Simulated AI Analysis.", e.getMessage());

            AiClassificationResult simulatedResult = new AiClassificationResult();

            if (title.toLowerCase().contains("database") || title.toLowerCase().contains("server")) {
                simulatedResult.setCategory("TECHNICAL");
                simulatedResult.setPriority("P1");
                simulatedResult.setConfidence(90);
                simulatedResult.setReply("Simulated AI: Critical infrastructure issue detected. Please check database connection limits immediately.");
            } else if (title.toLowerCase().contains("billing") || title.toLowerCase().contains("pay")) {
                simulatedResult.setCategory("BILLING");
                simulatedResult.setPriority("P3");
                simulatedResult.setConfidence(85);
                simulatedResult.setReply("Simulated AI: I have routed your billing inquiry to our finance team.");
            } else {
                simulatedResult.setCategory("GENERAL");
                simulatedResult.setPriority("P4");
                simulatedResult.setConfidence(70);
                simulatedResult.setReply("Simulated AI: Thank you for your ticket. An agent will review this shortly.");
            }

            return simulatedResult;
        }
    }
}