package com.rishanth.deskmind.dto;

import com.rishanth.deskmind.entity.TicketCategory;
import com.rishanth.deskmind.entity.TicketPriority;
import com.rishanth.deskmind.entity.TicketStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private String aiSuggestion;
    private Integer aiConfidence;
    private String customerName; // Don't expose the whole User object!
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}