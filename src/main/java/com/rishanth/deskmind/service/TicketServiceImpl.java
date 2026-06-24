package com.rishanth.deskmind.service;

import com.rishanth.deskmind.dto.AiClassificationResult;
import com.rishanth.deskmind.dto.TicketCreateRequest;
import com.rishanth.deskmind.dto.TicketResponse;
import com.rishanth.deskmind.entity.*;
import com.rishanth.deskmind.repository.TicketRepository;
import com.rishanth.deskmind.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request, String userEmail) {
        User customer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Initial Save (Default Status & Priority)
        Ticket ticket = Ticket.builder()
                .ticketNumber(generateTicketNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.P3) // Default
                .customer(customer)
                .build();

        ticket = ticketRepository.save(ticket);

        // 2. AI Classification Flow
        try {
            AiClassificationResult aiResult = aiService.classifyTicket(ticket.getTitle(), ticket.getDescription());

            if (aiResult != null) {
                ticket.setCategory(TicketCategory.valueOf(aiResult.getCategory()));
                ticket.setPriority(TicketPriority.valueOf(aiResult.getPriority()));
                ticket.setAiConfidence(aiResult.getConfidence());
                ticket.setAiSuggestion(aiResult.getReply());
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            // Failsafe: If AI returns garbage enum values or crashes
            log.warn("AI Classification generated invalid data for Ticket {}. Applying defaults.", ticket.getTicketNumber());
            ticket.setCategory(null);
            ticket.setPriority(TicketPriority.P3);
            ticket.setAiSuggestion(null);
        }

        // 3. Final Save & Return
        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Override
    public TicketResponse getTicketById(Long id, String userEmail) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Authorization check: Ensure customer owns the ticket
        if (!ticket.getCustomer().getEmail().equals(userEmail)) {
            throw new RuntimeException("Access Denied: You do not own this ticket.");
        }

        return mapToResponse(ticket);
    }

    @Override
    public List<TicketResponse> getCustomerTickets(String userEmail) {
        User customer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByCustomerId(customer.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private String generateTicketNumber() {
        // Format: TKT-2026-XXXXXX
        String year = String.valueOf(Year.now().getValue());
        String randomStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TKT-" + year + "-" + randomStr;
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .aiSuggestion(ticket.getAiSuggestion())
                .aiConfidence(ticket.getAiConfidence())
                .customerName(ticket.getCustomer().getName())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}