package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.entity.PasswordResetToken;
import com.rishanth.deskmind.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByOtpAndUser(String otp, User user);

    @Transactional
    @Modifying
    void deleteByUser(User user); // To clean up old tokens
}