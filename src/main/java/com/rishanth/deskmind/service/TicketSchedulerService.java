package com.rishanth.deskmind.service;

import com.rishanth.deskmind.entity.Ticket;
import com.rishanth.deskmind.entity.TicketStatus;
import com.rishanth.deskmind.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketSchedulerService {

    private final TicketRepository ticketRepository;
    private final AuditService auditService;

    // Cron expression: Runs every night at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoCloseResolvedTickets() {
        log.info("Running Auto-Close Ticket Scheduler...");

        // Find tickets that have been RESOLVED for more than 3 days
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        List<Ticket> oldTickets = ticketRepository.findByStatusAndUpdatedAtBefore(TicketStatus.RESOLVED, cutoff);

        for (Ticket ticket : oldTickets) {
            ticket.setStatus(TicketStatus.CLOSED);
            ticketRepository.save(ticket);

            // Log it in our Admin Ledger!
            auditService.logAction("TICKET_AUTOCLOSED", "Ticket", ticket.getId(), "SYSTEM", "Auto-closed after 3 days of inactivity");
        }

        if (!oldTickets.isEmpty()) {
            log.info("Auto-closed {} inactive tickets.", oldTickets.size());
        }
    }
}