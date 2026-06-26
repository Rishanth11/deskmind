package com.rishanth.deskmind.service;

import com.rishanth.deskmind.dto.AiClassificationResult;
import com.rishanth.deskmind.dto.TicketCreateRequest;
import com.rishanth.deskmind.dto.TicketReplyResponse;
import com.rishanth.deskmind.dto.TicketResponse;
import com.rishanth.deskmind.entity.*;
import com.rishanth.deskmind.repository.SlaConfigRepository;
import com.rishanth.deskmind.repository.TicketReplyRepository; // Make sure to create this if you haven't!
import com.rishanth.deskmind.repository.TicketRepository;
import com.rishanth.deskmind.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final TicketReplyRepository replyRepository;
    private final RoutingService routingService;
    private final SlaConfigRepository slaConfigRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request, String userEmail) {
        User customer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = Ticket.builder()
                .ticketNumber(generateTicketNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.P3)
                .customer(customer)
                .build();

        ticket = ticketRepository.save(ticket);

        try {
            log.info("Calling AI for ticket: {}", ticket.getTicketNumber());
            AiClassificationResult aiResult = aiService.classifyTicket(ticket.getTitle(), ticket.getDescription());
            log.info("AI returned: category={}, priority={}, confidence={}",
                        aiResult.getCategory(), aiResult.getPriority(), aiResult.getConfidence());
            if (aiResult != null) {
                ticket.setCategory(TicketCategory.valueOf(aiResult.getCategory()));
                ticket.setPriority(TicketPriority.valueOf(aiResult.getPriority()));
                ticket.setAiConfidence(aiResult.getConfidence());
                ticket.setAiSuggestion(aiResult.getReply());
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("AI result mapping failed: {}", e.getMessage());
            log.warn("AI Classification generated invalid data for Ticket {}. Applying defaults.", ticket.getTicketNumber());
            ticket.setCategory(null);
            ticket.setPriority(TicketPriority.P3);
            ticket.setAiSuggestion(null);
        }

        User assignedAgent = routingService.assignToBestAgent(ticket.getCategory());
        if (assignedAgent != null) {
            ticket.setAgent(assignedAgent);
            auditService.logAction("TICKET_ROUTED", "Ticket", ticket.getId(), "SYSTEM",
                    "Routed to " + assignedAgent.getEmail() + " via load balancer");
        }

        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);

    }

    @Override
    public TicketResponse getTicketById(Long id, String userEmail) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Allow access if the user is the customer OR an agent
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        if (user.getRole() == Role.CUSTOMER && !ticket.getCustomer().getEmail().equals(userEmail)) {
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

    // --- NEW: AGENT MODULE METHODS ---

    @Override
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll() // Replace with findAllByOrderByCreatedAtDesc() if you added it to repo
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketResponse assignTicketToAgent(Long ticketId, String agentEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        ticket.setAgent(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        ticket = ticketRepository.save(ticket);
        return mapToResponse(ticket);
    }

    @Override
    @Transactional
    public TicketResponse mergeTickets(Long sourceId, Long targetId, String agentEmail) {
        Ticket source = ticketRepository.findById(sourceId).orElseThrow(() -> new RuntimeException("Source ticket not found"));
        Ticket target = ticketRepository.findById(targetId).orElseThrow(() -> new RuntimeException("Target ticket not found"));
        User agent = userRepository.findByEmail(agentEmail).orElseThrow(() -> new RuntimeException("Agent not found"));

        // Move all replies from source to target
        List<TicketReply> sourceReplies = replyRepository.findByTicketIdOrderByCreatedAtAsc(sourceId);
        for (TicketReply reply : sourceReplies) {
            reply.setTicket(target);
        }
        replyRepository.saveAll(sourceReplies);

        // Add System Note to Target
        TicketReply targetNote = new TicketReply();
        targetNote.setTicket(target);
        targetNote.setSender(agent);
        targetNote.setInternal(true);
        targetNote.setMessage("System: Merged replies from ticket " + source.getTicketNumber());
        replyRepository.save(targetNote);

        // Close Source Ticket with Note
        TicketReply sourceNote = new TicketReply();
        sourceNote.setTicket(source);
        sourceNote.setSender(agent);
        sourceNote.setInternal(false);
        sourceNote.setMessage("System: This ticket has been merged into " + target.getTicketNumber() + " and closed.");
        replyRepository.save(sourceNote);

        source.setStatus(TicketStatus.CLOSED);
        ticketRepository.save(source);

        return mapToResponse(target);
    }

    // --- Helper Methods ---

    private String generateTicketNumber() {
        String year = String.valueOf(Year.now().getValue());
        String randomStr = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "TKT-" + year + "-" + randomStr;
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        LocalDateTime deadline = calculateSlaDeadline(ticket.getPriority(), ticket.getCreatedAt());
        boolean isBreached = ticket.getStatus() != TicketStatus.RESOLVED
                && ticket.getStatus() != TicketStatus.CLOSED
                && LocalDateTime.now().isAfter(deadline);

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
                // Agent mapping
                .agentId(ticket.getAgent() != null ? ticket.getAgent().getId() : null)
                .agentName(ticket.getAgent() != null ? ticket.getAgent().getName() : "Unassigned")
                // SLA mapping
                .slaDeadline(deadline)
                .slaBreached(isBreached)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    private LocalDateTime calculateSlaDeadline(TicketPriority priority, LocalDateTime createdAt) {
        if (createdAt == null) return LocalDateTime.now();

        SlaConfig config = slaConfigRepository.findByPriority(priority);
        // Fallback to default hardcoded values if the Admin hasn't configured them in the DB yet
        int hours = 24;
        if (config != null) {
            hours = config.getDeadlineHours();
        } else {
            hours = switch (priority) {
                case P1 -> 1;
                case P2 -> 4;
                case P3 -> 24;
                case P4 -> 72;
                default -> 24;
            };
        }
        return createdAt.plusHours(hours);
    }

    // ==========================================
    // CHAT & REPLY SYSTEM LOGIC
    // ==========================================

    @Override
    public List<TicketReplyResponse> getReplies(Long ticketId, User requestingUser) {
        List<TicketReply> replies = replyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

        return replies.stream()
                .filter(reply -> {
                    // Customers cannot see internal notes. Agents and Admins can see everything.
                    if (requestingUser.getRole() == Role.CUSTOMER) {
                        return !reply.isInternal();
                    }
                    return true;
                })
                .map(this::mapReplyToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketReplyResponse addReply(Long ticketId, String userEmail, String message, boolean isInternal) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        User sender = userRepository.findByEmail(userEmail).orElseThrow();

        TicketReply reply = new TicketReply();
        reply.setTicket(ticket);
        reply.setSender(sender);
        reply.setMessage(message);
        reply.setInternal(isInternal);
        reply = replyRepository.save(reply);

        // Auto-Status Logic: If an agent sends a public reply to an OPEN ticket, move it to IN_PROGRESS
        if (sender.getRole() == Role.AGENT && ticket.getStatus() == TicketStatus.OPEN && !isInternal) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticketRepository.save(ticket);
        }

        return mapReplyToResponse(reply);
    }

    // Helper method to convert the Entity to the DTO
    private TicketReplyResponse mapReplyToResponse(TicketReply reply) {
        return TicketReplyResponse.builder()
                .id(reply.getId())
                .senderName(reply.getSender().getName())
                .message(reply.getMessage())
                .internal(reply.isInternal())
                .createdAt(reply.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public TicketResponse updateStatus(Long ticketId, TicketStatus newStatus, String performedBy) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        // Log the action to the Audit Ledger
        auditService.logAction(
                "STATUS_CHANGED",
                "Ticket",
                ticket.getId(),
                performedBy,
                "Changed status from " + oldStatus + " to " + newStatus
        );

        return mapToResponse(savedTicket);
    }
}