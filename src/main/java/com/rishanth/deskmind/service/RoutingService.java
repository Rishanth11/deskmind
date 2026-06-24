package com.rishanth.deskmind.service;

import com.rishanth.deskmind.entity.*;
import com.rishanth.deskmind.repository.TeamRepository;
import com.rishanth.deskmind.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {
    private final TeamRepository teamRepository;
    private final TicketRepository ticketRepository;

    public User assignToBestAgent(TicketCategory category) {
        if (category == null) return null;

        Team team = teamRepository.findByHandlesCategory(category);
        if (team == null || team.getAgents() == null || team.getAgents().isEmpty()) {
            log.warn("No team or agents available for category: {}", category);
            return null;
        }

        User bestAgent = null;
        long minOpenTickets = Long.MAX_VALUE;

        // LOAD BALANCER: Find the agent with the fewest open/in-progress tickets
        for (User agent : team.getAgents()) {
            long currentLoad = ticketRepository.countByAgentAndStatusIn(
                    agent, List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS)
            );
            if (currentLoad < minOpenTickets) {
                minOpenTickets = currentLoad;
                bestAgent = agent;
            }
        }
        return bestAgent;
    }
}