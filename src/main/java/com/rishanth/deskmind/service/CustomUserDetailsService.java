package com.rishanth.deskmind.service;

import com.rishanth.deskmind.entity.User;
import com.rishanth.deskmind.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuditService auditService; // NEW: Added the AuditService

    // NEW: Inject both repositories via the constructor
    public CustomUserDetailsService(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public void updateAvailability(String email, boolean isAvailable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAvailable(isAvailable);
        userRepository.save(user);

        // Log this in our system audit trail!
        String statusText = isAvailable ? "Online (Accepting Tickets)" : "Offline (Routing Paused)";
        auditService.logAction("PRESENCE_CHANGED", "User", user.getId(), user.getName(), "Agent status changed to: " + statusText);
    }
}