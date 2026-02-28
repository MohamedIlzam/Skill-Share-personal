package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.auth.RegisterRequest;
import com.skillshare.skillshare.exception.ResourceConflictException;
import com.skillshare.skillshare.service.auth.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthWebController {

    private final AuthService authService;

    public AuthWebController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") RegisterRequest request, Model model) {
        try {
            authService.registerUser(request.getFullName(), request.getEmail(), request.getPassword());
            return "redirect:/login?registered";
        } catch (ResourceConflictException e) {
            model.addAttribute("error", "Email already exists!");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration.");
            return "register";
        }
    }
}
