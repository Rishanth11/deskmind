package com.rishanth.deskmind.controller;

import com.rishanth.deskmind.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CustomUserDetailsService userService;

    // Inject your service here
    public UserController(CustomUserDetailsService userService) {
        this.userService = userService;
    }

    // This catches the PUT request from the React AgentDashboard
    @PutMapping("/availability")
    @PreAuthorize("hasRole('AGENT')")
    public ResponseEntity<Void> updateAvailability(@RequestParam boolean available, Principal principal) {
        // principal.getName() automatically gets the email of the currently logged-in user!
        userService.updateAvailability(principal.getName(), available);
        return ResponseEntity.ok().build();
    }
}