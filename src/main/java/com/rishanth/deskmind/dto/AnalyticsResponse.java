package com.rishanth.deskmind.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AnalyticsResponse {
    private long totalOpenTickets;
    private long resolvedToday;
    private long slaBreaches;
    private List<DailyCount> ticketsPerDay;
    private List<CategoryCount> ticketsByCategory;
    private List<AgentPerformance> agentPerformance;

    // Interface Projections to cleanly map SQL results
    public interface DailyCount { String getDate(); Long getCount(); }
    public interface CategoryCount { String getCategory(); Long getCount(); }
    public interface AgentPerformance { String getAgentName(); Long getTicketsHandled(); }
}