package com.rishanth.deskmind.security;

import com.rishanth.deskmind.entity.Role;
import com.rishanth.deskmind.entity.User;
import com.rishanth.deskmind.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if our super admin already exists
        if (userRepository.findByEmail("admin@deskmind.com").isEmpty()) {

            User superAdmin = new User();
            superAdmin.setName("SuperAdmin");
            superAdmin.setEmail("admin@deskmind.com");
            // ALWAYS hash the seeded password!
            superAdmin.setPassword(passwordEncoder.encode("superadmin111"));
            superAdmin.setRole(Role.ADMIN); // Assuming you have a Role enum

            userRepository.save(superAdmin);
            System.out.println("✅ Super Admin successfully seeded!");
        }
    }
}