package com.rishanth.deskmind.service;

import com.rishanth.deskmind.dto.TicketCreateRequest;
import com.rishanth.deskmind.dto.TicketResponse;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(TicketCreateRequest request, String userEmail);
    TicketResponse getTicketById(Long id, String userEmail);
    List<TicketResponse> getCustomerTickets(String userEmail);
}