package com.rishanth.deskmind.service;

import com.rishanth.deskmind.dto.TicketCreateRequest;
import com.rishanth.deskmind.dto.TicketReplyResponse;
import com.rishanth.deskmind.dto.TicketResponse;
import com.rishanth.deskmind.entity.TicketStatus;
import com.rishanth.deskmind.entity.User;

import java.util.List;

public interface TicketService {
    TicketResponse createTicket(TicketCreateRequest request, String userEmail);
    TicketResponse getTicketById(Long id, String userEmail);
    List<TicketResponse> getCustomerTickets(String userEmail);

    List<TicketResponse> getAllTickets();
    TicketResponse assignTicketToAgent(Long ticketId, String agentEmail);
    TicketResponse mergeTickets(Long sourceId, Long targetId, String agentEmail);

    List<TicketReplyResponse> getReplies(Long ticketId, User requestingUser);
    TicketReplyResponse addReply(Long ticketId, String userEmail, String message, boolean isInternal);

    TicketResponse updateStatus(Long ticketId, TicketStatus newStatus, String performedBy);

    List<TicketResponse> getAgentTickets(String agentEmail);
}