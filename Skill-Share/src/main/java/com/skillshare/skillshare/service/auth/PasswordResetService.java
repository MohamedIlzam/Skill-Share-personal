package com.skillshare.skillshare.service.auth;

import com.skillshare.skillshare.dto.auth.ResetPasswordDTO;
import com.skillshare.skillshare.model.user.PasswordResetToken;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.PasswordResetTokenRepository;
import com.skillshare.skillshare.repository.UserRepository;
import com.skillshare.skillshare.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${server.port:8080}")
    private String serverPort;

    @Transactional
    public void generateResetToken(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // SECURITY FIX: Block password reset for deactivated accounts
            if (user.getStatus() != com.skillshare.skillshare.model.user.UserStatus.ACTIVE) {
                logger.info("Password reset blocked for inactive user account: {}", normalizedEmail);
                return; // Silent return to prevent enumeration
            }

            // Delete any existing tokens for this user
            tokenRepository.deleteByUser(user);
            
            // Create and save new token
            PasswordResetToken resetToken = new PasswordResetToken(user);
            tokenRepository.save(resetToken);
            
            // Send email
            String resetLink = "http://localhost:" + serverPort + "/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordResetEmail(normalizedEmail, resetLink);
            
            logger.info("Password reset email dispatched to: {}", normalizedEmail);
        } else {
            logger.info("Password reset requested for unregistered email: {}", normalizedEmail);
        }
    }

    public boolean validatePasswordResetToken(String token) {
        return tokenRepository.findByToken(token)
                .map(resetToken -> !resetToken.isExpired())
                .orElse(false);
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO request) {
        if (!request.isPasswordMatch()) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token."));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Password reset token has expired.");
        }

        User user = resetToken.getUser();
        
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);

        // Delete the token so it cannot be reused
        tokenRepository.delete(resetToken);
    }
}
