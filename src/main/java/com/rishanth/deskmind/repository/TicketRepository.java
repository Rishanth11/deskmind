package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.entity.Ticket;
import com.rishanth.deskmind.entity.TicketCategory;
import com.rishanth.deskmind.entity.TicketPriority;
import com.rishanth.deskmind.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerId(Long customerId);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByCategory(TicketCategory category);
    List<Ticket> findByPriority(TicketPriority priority);
}