package com.rishanth.deskmind.controller;

import com.rishanth.deskmind.dto.*;
import com.rishanth.deskmind.entity.PasswordResetToken;
import com.rishanth.deskmind.entity.Role;
import com.rishanth.deskmind.entity.User;
import com.rishanth.deskmind.repository.PasswordResetTokenRepository;
import com.rishanth.deskmind.repository.UserRepository;
import com.rishanth.deskmind.security.JwtService;
import com.rishanth.deskmind.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, PasswordResetTokenRepository tokenRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Default role based on FR-AUTH-06 context, or set by admin later
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // This is the line that crashes if the email/password is wrong
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Catch wrong passwords or missing emails gracefully!
            System.out.println("❌ LOGIN FAILED: Bad credentials for email -> " + request.getEmail());
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        } catch (Exception e) {
            System.out.println("❌ LOGIN ERROR: " + e.getMessage());
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred during authentication");
        }

        // If we survive the try-catch, the user is legit!
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);

        System.out.println("✅ LOGIN SUCCESS: User " + user.getEmail() + " logged in as " + user.getRole().name());
        return org.springframework.http.ResponseEntity.ok(new AuthResponse(jwtToken, user.getRole().name()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Clean up any existing tokens for this user
            tokenRepository.deleteByUser(user);

            // Generate and save new OTP
            String otp = emailService.generateOtp();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setOtp(otp);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // 10 min validity
            tokenRepository.save(resetToken);

            // Send email
            emailService.sendOtpEmail(user.getEmail(), otp);
        }

        // Always return generic message to prevent email enumeration attacks
        return ResponseEntity.ok("If an account with that email exists, an OTP has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetToken resetToken = tokenRepository.findByOtpAndUser(request.getOtp(), user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (resetToken.isExpired()) {
            return ResponseEntity.badRequest().body("OTP has expired.");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete the used token
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok("Password has been reset successfully.");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}