package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.dto.AnalyticsResponse;
import com.rishanth.deskmind.entity.Ticket;
import com.rishanth.deskmind.entity.TicketCategory;
import com.rishanth.deskmind.entity.TicketPriority;
import com.rishanth.deskmind.entity.TicketStatus;
import com.rishanth.deskmind.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    // ... existing repository methods ...

    // Analytics: Status counts
    long countByStatus(TicketStatus status);

    // Analytics: SLA Breaches
    // Analytics: SLA Breaches (Using a Native SQL query since slaBreached is calculated dynamically)
    @Query(value = "SELECT COUNT(*) FROM tickets WHERE status != 'RESOLVED' AND created_at < NOW() - INTERVAL 24 HOUR", nativeQuery = true)
    long countActiveSlaBreaches();

    // Analytics: Resolved Today
    @Query(value = "SELECT COUNT(*) FROM tickets WHERE status = 'RESOLVED' AND DATE(updated_at) = CURRENT_DATE", nativeQuery = true)
    long countResolvedToday();

    // Analytics: Tickets Per Day (Last 30 Days)
    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as count FROM tickets WHERE created_at >= CURRENT_DATE - INTERVAL 30 DAY GROUP BY DATE(created_at) ORDER BY date ASC", nativeQuery = true)
    List<AnalyticsResponse.DailyCount> countTicketsPerDayLast30Days();

    // Analytics: Category Distribution
    @Query(value = "SELECT category as category, COUNT(*) as count FROM tickets GROUP BY category", nativeQuery = true)
    List<AnalyticsResponse.CategoryCount> countTicketsByCategory();

    // Analytics: Agent Performance
    @Query(value = "SELECT u.name as agentName, COUNT(t.id) as ticketsHandled FROM users u JOIN tickets t ON u.id = t.agent_id WHERE t.status = 'RESOLVED' GROUP BY u.id", nativeQuery = true)
    List<AnalyticsResponse.AgentPerformance> getAgentPerformance();
}