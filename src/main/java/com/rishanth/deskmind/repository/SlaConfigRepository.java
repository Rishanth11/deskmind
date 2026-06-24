package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.entity.SlaConfig;
import com.rishanth.deskmind.entity.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaConfigRepository extends JpaRepository<SlaConfig, Long> {
    SlaConfig findByPriority(TicketPriority priority);
}