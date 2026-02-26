package com.skillshare.skillshare.service.user;

import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createForSmokeTest(String fullName, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();

        // idempotent: don't insert duplicates on every run
        return userRepository.findByEmail(normalizedEmail).orElseGet(() -> {
            String hash = passwordEncoder.encode(rawPassword);
            User user = User.register(fullName, normalizedEmail, hash);
            return userRepository.save(user);
        });
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("User not found: " + email));
    }
}
