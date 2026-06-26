package com.rishanth.deskmind.dto;

import java.util.List;

public class TeamDTO {
    private Long id;
    private String name;
    private String handlesCategory;
    private List<AgentDTO> agents; // 🚨 CHANGE 1: This is now a list of AgentDTOs

    public TeamDTO(Long id, String name, String handlesCategory, List<AgentDTO> agents) {
        this.id = id;
        this.name = name;
        this.handlesCategory = handlesCategory;
        this.agents = agents;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getHandlesCategory() { return handlesCategory; }
    public List<AgentDTO> getAgents() { return agents; } // 🚨 CHANGE 2: Update getter
}