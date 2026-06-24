package com.rishanth.deskmind.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;      // e.g., "ROLE_CHANGED", "TICKET_ROUTED"
    private String entityName;  // e.g., "User", "Ticket"
    private Long entityId;
    private String performedBy; // Email of the admin/system
    private String details;     // e.g., "Assigned Agent1 to Technical Team"

    private LocalDateTime timestamp = LocalDateTime.now();
}