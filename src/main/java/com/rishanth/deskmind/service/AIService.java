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
            You are an expert IT Helpdesk Triage AI working for a B2B SaaS company.
            Your job is to analyze support tickets and strictly classify them.
            
            Title: %s
            Description: %s
            
            === CLASSIFICATION RULES ===
            
            1. CATEGORY (pick exactly one):
               - TECHNICAL: Code bugs, server crashes, database errors, software exceptions, API failures.
               - BILLING: Invoices, payments, refunds, subscriptions, pricing questions.
               - ACCOUNT: Passwords, login issues, 2FA, permissions, access requests.
               - GENERAL: Non-technical questions, feedback, feature requests, or unclear requests.
            
            2. PRIORITY (pick exactly one, guided by category):
               - P1 (Critical): System crashes, total outages, data loss, software exceptions affecting production.
                                TECHNICAL tickets only. Never assign P1 to BILLING, ACCOUNT, or GENERAL.
               - P2 (High): Major feature broken, urgent account lockouts affecting multiple users.
                            Applies to TECHNICAL or ACCOUNT tickets only.
               - P3 (Medium): Standard bugs, billing requests, single-user account issues, general support.
               - P4 (Low): Feature requests, general questions, low-impact UI issues, vague feedback.
            
            3. CONFIDENCE: A number 0-100 reflecting how certain you are about this classification.
               - 90-100: Very clear-cut ticket.
               - 60-89: Reasonably clear but some ambiguity.
               - Below 60: Ticket is vague or could fit multiple categories. Flag for human review.
            
            4. REPLY: Write a 1-2 sentence professional response to the user.
               - Acknowledge their specific issue (mention the actual problem, not generic text).
               - State which team will handle it and the expected response time based on priority:
                 P1 = within 1 hour, P2 = within 4 hours, P3 = within 1 business day, P4 = within 3 business days.
            
            === EXAMPLE ===
            Title: "NullPointerException in payment service after latest deployment"
            Description: "Our payment API started throwing NPEs after the v2.3 deploy. Transactions are failing."
            Output:
            {
                "category": "TECHNICAL",
                "priority": "P1",
                "confidence": 97,
                "reply": "We've identified this as a critical production issue and our engineering team has been alerted immediately — expect a response within 1 hour."
            }
            """, title, description);

        try {
            log.info("Sending ticket to AI for classification...");

            // The .entity() method automatically instructs the LLM to use structured JSON output
            AiClassificationResult result = chatClient.prompt()
                    .user(promptText)
                    .call()
                    .entity(AiClassificationResult.class);

            // Route low-confidence results for human review safely
            if (result.getConfidence() < 60) {
                log.warn("Low confidence classification ({}) for ticket: '{}'. Flagging for human review.",
                        result.getConfidence(), title);
                // FIXED: Default to P3 so it doesn't get buried if it's actually important!
                result.setCategory("GENERAL");
                result.setPriority("P3");
                result.setReply("Your ticket has been received and flagged for priority manual review by our team. We will get back to you within 1 business day.");
            }

            return result;

        } catch (Exception e) {
            log.warn("AI Error: {}. Falling back to simulated analysis.", e.getMessage());

            AiClassificationResult fallback = new AiClassificationResult();
            String titleLower = title.toLowerCase();

            if (titleLower.contains("exception") || titleLower.contains("crash") ||
                    titleLower.contains("database") || titleLower.contains("server") ||
                    titleLower.contains("outage")) {
                fallback.setCategory("TECHNICAL");
                fallback.setPriority("P1");
                fallback.setConfidence(90);
                fallback.setReply("We've detected a critical infrastructure issue and our engineering team has been alerted — expect a response within 1 hour.");

            } else if (titleLower.contains("login") || titleLower.contains("password") ||
                    titleLower.contains("access") || titleLower.contains("locked")) {
                fallback.setCategory("ACCOUNT");
                fallback.setPriority("P2");
                fallback.setConfidence(85);
                fallback.setReply("Your account access issue has been escalated to our support team and will be resolved within 4 hours.");

            } else if (titleLower.contains("billing") || titleLower.contains("invoice") ||
                    titleLower.contains("payment") || titleLower.contains("refund")) {
                fallback.setCategory("BILLING");
                fallback.setPriority("P3");
                fallback.setConfidence(85);
                fallback.setReply("Your billing inquiry has been routed to our finance team and will be addressed within 1 business day.");

            } else {
                fallback.setCategory("GENERAL");
                // Fallback default is also P3 to ensure no tickets get lost
                fallback.setPriority("P3");
                fallback.setConfidence(70);
                fallback.setReply("Thank you for reaching out. Your ticket has been received and an agent will review it within 1 business day.");
            }

            return fallback;
        }
    }
}