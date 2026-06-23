package com.rishanth.deskmind.controller;

import com.rishanth.deskmind.dto.RoleUpdateRequest;
import com.rishanth.deskmind.entity.Role;
import com.rishanth.deskmind.entity.User;
import com.rishanth.deskmind.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Only ADMINs can access routes in this controller
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/assign-role/{userId}")
    public ResponseEntity<?> assignRole(@PathVariable Long userId, @RequestBody RoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        try {
            Role newRole = Role.valueOf(request.getRole().toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
            return ResponseEntity.ok("Role updated successfully to " + newRole);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role provided. Accepted values: CUSTOMER, AGENT, MANAGER, ADMIN.");
        }
    }
}