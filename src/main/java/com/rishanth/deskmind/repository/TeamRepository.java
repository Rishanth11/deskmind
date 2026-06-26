package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.entity.Team;
import com.rishanth.deskmind.entity.Ticket;
import com.rishanth.deskmind.entity.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Team findByHandlesCategory(TicketCategory category);
}