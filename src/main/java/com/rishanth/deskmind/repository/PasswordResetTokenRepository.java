package com.rishanth.deskmind.repository;

import com.rishanth.deskmind.entity.PasswordResetToken;
import com.rishanth.deskmind.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByOtpAndUser(String otp, User user);
    void deleteByUser(User user); // To clean up old tokens
}