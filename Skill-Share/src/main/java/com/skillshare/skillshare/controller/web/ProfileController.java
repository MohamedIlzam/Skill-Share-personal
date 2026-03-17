package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.user.UserProfileDTO;
import com.skillshare.skillshare.dto.user.UserProfileUpdateDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.user.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public String showProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileDTO profile = userProfileService.getProfileByUserId(userDetails.getUser().getId());
        model.addAttribute("profile", profile);
        return "profile-view";
    }

    @GetMapping("/profile/edit")
    public String showEditForm(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserProfileDTO profile = userProfileService.getProfileByUserId(userDetails.getUser().getId());
        
        UserProfileUpdateDTO updateDTO = UserProfileUpdateDTO.builder()
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .location(profile.getLocation())
                .build();
        
        model.addAttribute("profileUpdateDTO", updateDTO);
        return "profile-edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(
            @Valid @ModelAttribute("profileUpdateDTO") UserProfileUpdateDTO updateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "profile-edit";
        }

        try {
            userProfileService.updateProfile(userDetails.getUser().getId(), updateDTO, updateDTO.getProfilePicture());
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("profilePicture", "error.profileUpdateDTO", e.getMessage());
            return "profile-edit";
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/availability/toggle")
    public String toggleAvailability(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes redirectAttributes) {
        userProfileService.toggleAvailability(userDetails.getUser().getId());
        redirectAttributes.addFlashAttribute("successMessage", "Availability status updated!");
        return "redirect:/profile";
    }
}
