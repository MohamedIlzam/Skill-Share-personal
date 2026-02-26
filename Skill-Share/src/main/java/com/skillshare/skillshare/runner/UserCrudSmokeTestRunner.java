package com.skillshare.skillshare.runner;

import com.skillshare.skillshare.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserCrudSmokeTestRunner implements CommandLineRunner {

    private final UserService userService;

    public UserCrudSmokeTestRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        String email = "test.user@skillshare.local";

        var createdOrExisting = userService.createForSmokeTest(
                "Test User",
                email,
                "Password123@#$"
        );

        var loaded = userService.getByEmail(email);

        System.out.println("✅ User CRUD Smoke Test: createdOrExistingId=" + createdOrExisting.getId()
                + " | loadedId=" + loaded.getId()
                + " | email=" + loaded.getEmail());
    }
}
