package com.rishanth.deskmind.controller;

import com.rishanth.deskmind.dto.AgentDTO;
import com.rishanth.deskmind.dto.TeamDTO;
import com.rishanth.deskmind.entity.*;
import com.rishanth.deskmind.repository.*;
import com.rishanth.deskmind.service.AuditService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Secures the entire controller
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SlaConfigRepository slaConfigRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    // --- SLA Management ---
    @PostMapping("/sla")
    public ResponseEntity<SlaConfig> configureSla(@RequestBody SlaConfig config, Principal principal) {
        SlaConfig existing = slaConfigRepository.findByPriority(config.getPriority());
        if (existing != null) {
            existing.setDeadlineHours(config.getDeadlineHours());
            config = slaConfigRepository.save(existing);
        } else {
            config = slaConfigRepository.save(config);
        }

        auditService.logAction("SLA_UPDATED", "SlaConfig", config.getId(), principal.getName(),
                "Updated " + config.getPriority() + " to " + config.getDeadlineHours() + " hours");
        return ResponseEntity.ok(config);
    }

    // --- Team & Routing Management ---
    @PostMapping("/teams")
    public ResponseEntity<?> createTeam(@RequestBody Team team, Principal principal) {
        try {
            Team savedTeam = teamRepository.save(team);
            auditService.logAction("TEAM_CREATED", "Team", savedTeam.getId(), principal.getName(), "Created team: " + team.getName());
            return ResponseEntity.ok(savedTeam);
        } catch (Exception e) {
            e.printStackTrace(); // 🚨 THIS WILL FORCE THE ERROR INTO YOUR RENDER LOGS
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/teams/{teamId}/agents/{userId}")
    public ResponseEntity<Team> addAgentToTeam(@PathVariable Long teamId, @PathVariable Long userId, Principal principal) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        User agent = userRepository.findById(userId).orElseThrow();

        team.getAgents().add(agent);
        team = teamRepository.save(team);

        auditService.logAction("AGENT_ASSIGNED", "Team", team.getId(), principal.getName(),
                "Added " + agent.getEmail() + " to team " + team.getName());
        return ResponseEntity.ok(team);
    }

    // --- User Roles ---
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<User> changeUserRole(@PathVariable Long userId, @RequestParam Role role, Principal principal) {
        User user = userRepository.findById(userId).orElseThrow();
        String oldRole = user.getRole().name();
        user.setRole(role);
        user = userRepository.save(user);

        auditService.logAction("ROLE_CHANGED", "User", user.getId(), principal.getName(),
                "Changed role from " + oldRole + " to " + role.name());
        return ResponseEntity.ok(user);
    }

    // --- Audit Logs ---
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByTimestampDesc());
    }

    @Transactional
    @GetMapping("/teams")
    public ResponseEntity<?> getAllTeams() {
        try {
            List<Team> teams = teamRepository.findAll();

            List<TeamDTO> teamDTOs = teams.stream().map(team -> {

                // 🚨 THE FIX: Package the actual Agent Name and Email to send to the frontend!
                List<AgentDTO> agentList = team.getAgents().stream()
                        .map(agent -> new AgentDTO(agent.getId(), agent.getName(), agent.getEmail()))
                        .collect(Collectors.toList());

                String categoryName = (team.getHandlesCategory() != null)
                        ? team.getHandlesCategory().name()
                        : "UNASSIGNED";

                return new TeamDTO(
                        team.getId(),
                        team.getName(),
                        categoryName,
                        agentList // Pass the new list here
                );

            }).collect(Collectors.toList());

            return ResponseEntity.ok(teamDTOs);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("BACKEND CRASH REASON: " + e.getMessage());
        }
    }

    @GetMapping("/slas")
    public ResponseEntity<List<SlaConfig>> getAllSlas() {
        return ResponseEntity.ok(slaConfigRepository.findAll());
    }

    @PostMapping("/staff")
    public ResponseEntity<User> createStaff(@RequestBody User request, Principal principal) {
        User newStaff = new User();
        newStaff.setName(request.getName());
        newStaff.setEmail(request.getEmail());

        // FIXED: Now we are actively hashing the password before saving!
        newStaff.setPassword(passwordEncoder.encode(request.getPassword()));

        newStaff.setRole(request.getRole());

        User savedUser = userRepository.save(newStaff);
        auditService.logAction("STAFF_CREATED", "User", savedUser.getId(), principal.getName(), "Created " + savedUser.getRole() + ": " + savedUser.getEmail());

        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/agents")
    public ResponseEntity<List<AgentDTO>> getAvailableAgents() {
        List<User> agents = userRepository.findByRole(Role.AGENT);

        List<AgentDTO> dtos = agents.stream()
                .map(agent -> new AgentDTO(agent.getId(), agent.getName(), agent.getEmail()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // ==========================================
    // NEW: STAFF MANAGEMENT ENDPOINTS
    // ==========================================

    // 1. Fetch all Staff (Agents + Managers)
    @GetMapping("/staff/all")
    public ResponseEntity<?> getAllStaffDetails() {
        List<User> allStaff = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.AGENT || u.getRole() == Role.MANAGER)
                .collect(Collectors.toList());

        // We reuse your AgentDTO concept here to send safe data to the frontend,
        // but we'll include their active status and role.
        List<Map<String, Object>> staffList = allStaff.stream().map(staff -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", staff.getId());
            map.put("name", staff.getName());
            map.put("email", staff.getEmail());
            map.put("role", staff.getRole().name());
            map.put("isActive", staff.isActive());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(staffList);
    }

    // 2. Toggle Block/Unblock Status
    @Transactional
    @PutMapping("/staff/{id}/toggle-status")
    public ResponseEntity<?> toggleStaffStatus(@PathVariable Long id) {
        User staff = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Flip the status (if true, becomes false. If false, becomes true)
        staff.setActive(!staff.isActive());
        userRepository.save(staff);

        return ResponseEntity.ok(Map.of("message", "Status updated successfully", "isActive", staff.isActive()));
    }

    // 3. Delete Staff completely
    @Transactional
    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
        User staff = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Crucial: We must remove them from any teams first to avoid Database Foreign Key crashes!
        if (staff.getRole() == Role.AGENT) {
            List<Team> allTeams = teamRepository.findAll();
            for (Team team : allTeams) {
                if (team.getAgents().contains(staff)) {
                    team.getAgents().remove(staff);
                    teamRepository.save(team);
                }
            }
        }

        userRepository.delete(staff);
        return ResponseEntity.ok(Map.of("message", "Staff member deleted successfully"));
    }

    // 4. Remove Agent from a Team (for reassignment)
    @Transactional
    @DeleteMapping("/teams/{teamId}/agents/{agentId}")
    public ResponseEntity<?> removeAgentFromTeam(@PathVariable Long teamId, @PathVariable Long agentId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        team.getAgents().remove(agent);
        teamRepository.save(team);

        return ResponseEntity.ok(Map.of("message", "Agent removed from team successfully"));
    }
}