package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.user.PasswordResetToken;
import com.skillshare.skillshare.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
    void deleteByExpiryDateBefore(Instant now);
}
