package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.entity.Role;
import com.rishanth.deskmind.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // NEW: The Upgraded Smart Load Balancer (Filters out offline agents)
    @Query(value = "SELECT u.* FROM users u " +
            "JOIN team_agents ta ON u.id = ta.user_id " +
            "JOIN teams t ON ta.team_id = t.id " +
            "LEFT JOIN tickets tk ON u.id = tk.agent_id AND tk.status = 'IN_PROGRESS' " +
            "WHERE t.handles_category = :category AND u.role = 'AGENT' AND u.is_available = true " +
            "GROUP BY u.id " +
            "ORDER BY COUNT(tk.id) ASC LIMIT 1", nativeQuery = true)
    Optional<User> findAvailableAgentWithLeastTickets(@Param("category") String category);

    List<User> findByRole(Role role);
}