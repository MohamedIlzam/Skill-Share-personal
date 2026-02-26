package com.skillshare.skillshare.controller;

import com.skillshare.skillshare.dto.auth.RegisterRequest;
import com.skillshare.skillshare.dto.auth.RegisterResponse;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest request) {
        // Just directly calling the smoke test method for our API test
        User newOrExisting = userService.createForSmokeTest(
                request.getFullName(),
                request.getEmail(),
                request.getPassword()
        );
        
        RegisterResponse response = new RegisterResponse();
        response.setId(newOrExisting.getId());
        response.setFullName(newOrExisting.getFullName());
        response.setEmail(newOrExisting.getEmail());
        
        return ResponseEntity.ok(response);
    }
}
