package com.skillshare.skillshare.config;

import com.skillshare.skillshare.service.user.UserProfileService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializerConfig {

    @Bean
    public CommandLineRunner initializeProfiles(UserProfileService userProfileService) {
        return args -> {
            userProfileService.createMissingProfiles();
        };
    }
}
