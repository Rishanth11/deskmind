package com.rishanth.deskmind.controller;

import com.rishanth.deskmind.dto.TicketCreateRequest;
import com.rishanth.deskmind.dto.TicketResponse;
import com.rishanth.deskmind.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateRequest request,
            Authentication authentication) {

        // authentication.getName() pulls the email from the JWT Subject
        TicketResponse response = ticketService.createTicket(request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<TicketResponse>> getMyTickets(Authentication authentication) {
        List<TicketResponse> tickets = ticketService.getCustomerTickets(authentication.getName());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long id,
            Authentication authentication) {

        TicketResponse ticket = ticketService.getTicketById(id, authentication.getName());
        return ResponseEntity.ok(ticket);
    }
}