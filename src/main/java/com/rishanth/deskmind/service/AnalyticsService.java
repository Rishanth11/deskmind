package com.rishanth.deskmind.service;

import com.rishanth.deskmind.dto.AnalyticsResponse;
import com.rishanth.deskmind.entity.Ticket;
import com.rishanth.deskmind.entity.TicketStatus;
import com.rishanth.deskmind.repository.TicketRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TicketRepository ticketRepository;

    public AnalyticsResponse getDashboardMetrics() {
        return AnalyticsResponse.builder()
                .totalOpenTickets(ticketRepository.countByStatus(TicketStatus.OPEN))
                .resolvedToday(ticketRepository.countResolvedToday())
                .slaBreaches(ticketRepository.countActiveSlaBreaches())
                .ticketsPerDay(ticketRepository.countTicketsPerDayLast30Days())
                .ticketsByCategory(ticketRepository.countTicketsByCategory())
                .agentPerformance(ticketRepository.getAgentPerformance())
                .build();
    }

    public void exportTicketsToCsv(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"deskmind_tickets_export.csv\"");

        PrintWriter writer = response.getWriter();
        // CSV Header
        writer.println("Ticket ID,Ticket Number,Category,Priority,Status,Agent,Created At");

        List<Ticket> tickets = ticketRepository.findAll();
        for (Ticket t : tickets) {
            String agentName = (t.getAgent() != null) ? t.getAgent().getName() : "Unassigned";
            String category = (t.getCategory() != null) ? t.getCategory().name() : "N/A";

            // Format line and escape commas in text if necessary
            writer.printf("%d,%s,%s,%s,%s,%s,%s\n",
                    t.getId(),
                    t.getTicketNumber(),
                    category,
                    t.getPriority().name(),
                    t.getStatus().name(),
                    agentName,
                    t.getCreatedAt().toString()
            );
        }
        writer.flush();
        writer.close();
    }
}