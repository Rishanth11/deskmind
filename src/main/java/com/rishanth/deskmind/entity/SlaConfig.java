package com.rishanth.deskmind.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sla_configs")
@Data
public class SlaConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TicketPriority priority;

    @Column(name = "deadline_hours", nullable = false)
    private int deadlineHours;
}