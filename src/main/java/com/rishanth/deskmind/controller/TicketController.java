package com.rishanth.deskmind.controller;

import com.rishanth.deskmind.dto.ReplyRequest;
import com.rishanth.deskmind.dto.TicketCreateRequest;
import com.rishanth.deskmind.dto.TicketReplyResponse;
import com.rishanth.deskmind.dto.TicketResponse;
import com.rishanth.deskmind.entity.TicketStatus;
import com.rishanth.deskmind.entity.User;
import com.rishanth.deskmind.repository.UserRepository;
import com.rishanth.deskmind.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    // ==========================================
    // CUSTOMER ENDPOINTS
    // ==========================================

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody TicketCreateRequest request, Principal principal) {
        return ResponseEntity.ok(ticketService.createTicket(request, principal.getName()));
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketResponse>> getMyTickets(Principal principal) {
        return ResponseEntity.ok(ticketService.getCustomerTickets(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(ticketService.getTicketById(id, principal.getName()));
    }

    // ==========================================
    // AGENT & ADMIN ENDPOINTS
    // ==========================================

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTicket(@PathVariable Long id, Principal principal) {
        // The principal.getName() is the email of the Agent clicking the button
        return ResponseEntity.ok(ticketService.assignTicketToAgent(id, principal.getName()));
    }

    @PostMapping("/{sourceId}/merge/{targetId}")
    public ResponseEntity<TicketResponse> mergeTickets(
            @PathVariable Long sourceId,
            @PathVariable Long targetId,
            Principal principal) {
        return ResponseEntity.ok(ticketService.mergeTickets(sourceId, targetId, principal.getName()));
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<TicketReplyResponse>> getReplies(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        return ResponseEntity.ok(ticketService.getReplies(id, user));
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<TicketReplyResponse> addReply(
            @PathVariable Long id,
            @RequestBody ReplyRequest request,
            Principal principal) {
        return ResponseEntity.ok(ticketService.addReply(
                id,
                principal.getName(),
                request.getMessage(),
                request.isInternal()
        ));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'MANAGER')")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam TicketStatus status,
            Principal principal) {
        return ResponseEntity.ok(ticketService.updateStatus(id, status, principal.getName()));
    }

    @GetMapping("/agent")
    @PreAuthorize("hasAnyRole('AGENT')")
    public ResponseEntity<List<TicketResponse>> getAgentTickets(Principal principal) {
        return ResponseEntity.ok(ticketService.getAgentTickets(principal.getName()));
    }

    @PutMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<TicketResponse> escalateTicket(
            @PathVariable Long id,
            @RequestParam Long agentId,
            Principal principal) {
        return ResponseEntity.ok(ticketService.escalateTicket(id, agentId, principal.getName()));
    }
}