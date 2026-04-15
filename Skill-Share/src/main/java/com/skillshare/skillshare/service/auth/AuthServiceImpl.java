package com.skillshare.skillshare.service.auth;

import com.skillshare.skillshare.exception.ResourceConflictException;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.model.user.UserProfile;
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
        
        // Create profile during registration (Step 1 requirement)
        UserProfile profile = new UserProfile(user, fullName);
        user.setProfile(profile);

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, com.skillshare.skillshare.dto.auth.ChangePasswordDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.skillshare.skillshare.exception.ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("New password cannot be the same as the current password.");
        }

        if (!request.isPasswordMatch()) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);
    }
}
