package com.rishanth.deskmind.dto;

import java.util.List;

public class TeamDTO {
    private Long id;
    private String name;
    private String handlesCategory;
    private List<Long> agents; // We only send the IDs of the agents, not the whole User object!

    public TeamDTO(Long id, String name, String handlesCategory, List<Long> agents) {
        this.id = id;
        this.name = name;
        this.handlesCategory = handlesCategory;
        this.agents = agents;
    }

    // Getters are required for Spring Boot to convert this to JSON!
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getHandlesCategory() { return handlesCategory; }
    public List<Long> getAgents() { return agents; }
}