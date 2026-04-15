package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.auth.ResetPasswordDTO;
import com.skillshare.skillshare.service.auth.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        if (email == null || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Email is required.");
            return "redirect:/forgot-password";
        }
        
        passwordResetService.generateResetToken(email);
        
        // Generic success message to prevent user enumeration
        redirectAttributes.addFlashAttribute("success", "If an account with that email exists, a password reset link has been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "token", required = false) String token, Model model) {
        if (token == null || !passwordResetService.validatePasswordResetToken(token)) {
            model.addAttribute("error", "Invalid or expired password reset token.");
            return "login"; // Display error on the login page or a generic error page
        }

        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setToken(token);
        
        if (!model.containsAttribute("resetPasswordDTO")) {
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
        }
        
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO resetPasswordDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.resetPasswordDTO", bindingResult);
            redirectAttributes.addFlashAttribute("resetPasswordDTO", resetPasswordDTO);
            return "redirect:/reset-password?token=" + resetPasswordDTO.getToken();
        }

        try {
            passwordResetService.resetPassword(resetPasswordDTO);
            redirectAttributes.addFlashAttribute("registered", "Password has been reset successfully. Please log in."); // Using existing 'registered' param styling from login.html
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("resetPasswordDTO", resetPasswordDTO);
            return "redirect:/reset-password?token=" + resetPasswordDTO.getToken();
        }
    }
}
