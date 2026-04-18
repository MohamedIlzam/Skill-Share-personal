package com.skillshare.skillshare.repository;

import com.skillshare.skillshare.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.profile IS NULL")
    java.util.List<User> findAllUsersWithoutProfile();

    java.util.List<User> findAllByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email);

    long countByRole(com.skillshare.skillshare.model.user.Role role);
}
