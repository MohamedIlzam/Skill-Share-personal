package com.skillshare.skillshare.config;

import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.model.user.UserProfile;
import com.skillshare.skillshare.repository.UserRepository;
import com.skillshare.skillshare.service.user.UserProfileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializerConfig {

    @Bean
    public CommandLineRunner initializeData(UserRepository userRepository, 
                                          UserProfileService userProfileService,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default admin if not existing
            String adminEmail = "admin@skillshare.com";
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.register("System Administrator", adminEmail, passwordEncoder.encode("admin123"));
                admin.promoteToAdmin();
                
                UserProfile profile = new UserProfile(admin, "System Administrator");
                admin.setProfile(profile);
                
                userRepository.save(admin);
            }

            // Ensure all users have profiles
            userProfileService.createMissingProfiles();
        };
    }
}
