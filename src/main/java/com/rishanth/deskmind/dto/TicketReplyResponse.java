package com.rishanth.deskmind.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TicketReplyResponse {
    private Long id;
    private String senderName;
    private String message;
    private boolean internal;
    private LocalDateTime createdAt;
}