package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String showProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        // userDetails will not be null if the user is authenticated (handled by SecurityConfig)
        model.addAttribute("fullName", userDetails.getUser().getFullName());
        model.addAttribute("email", userDetails.getUser().getEmail());
        model.addAttribute("role", userDetails.getUser().getRole());
        
        return "profile";
    }
}
