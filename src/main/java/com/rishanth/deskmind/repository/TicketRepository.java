package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.entity.Ticket;
import com.rishanth.deskmind.entity.TicketCategory;
import com.rishanth.deskmind.entity.TicketPriority;
import com.rishanth.deskmind.entity.TicketStatus;
import com.rishanth.deskmind.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerId(Long customerId);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByCategory(TicketCategory category);
    List<Ticket> findByPriority(TicketPriority priority);

    List<Ticket> findAllByOrderByCreatedAtDesc();
    List<Ticket> findByStatusAndUpdatedAtBefore(TicketStatus status, LocalDateTime date);

    // This is the magic Spring Data method your RoutingService is looking for
    long countByAgentAndStatusIn(User agent, List<TicketStatus> statuses);
}