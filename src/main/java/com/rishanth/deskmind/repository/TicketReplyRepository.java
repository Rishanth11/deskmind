package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.entity.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketReplyRepository extends JpaRepository<TicketReply, Long> {
    // This fetches all replies for a ticket in chronological order
    List<TicketReply> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}