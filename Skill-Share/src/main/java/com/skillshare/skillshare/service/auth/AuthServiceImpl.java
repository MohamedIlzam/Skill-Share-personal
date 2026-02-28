package com.skillshare.skillshare.service.auth;

import com.skillshare.skillshare.exception.ResourceConflictException;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(String fullName, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResourceConflictException("Email already exists: " + normalizedEmail);
        }

        String hash = passwordEncoder.encode(rawPassword);
        User user = User.register(fullName, normalizedEmail, hash);
        
        return userRepository.save(user);
    }
}
