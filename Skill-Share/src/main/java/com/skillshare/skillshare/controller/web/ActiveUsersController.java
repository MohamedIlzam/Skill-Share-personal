package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.user.PublicUserDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.user.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ActiveUsersController {

    private final UserProfileService userProfileService;

    @GetMapping("/active-users")
    public String listActiveUsers(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "view", defaultValue = "users") String view,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long currentUserId = (userDetails != null) ? userDetails.getUser().getId() : -1L;
        
        if ("users".equals(view)) {
            Page<PublicUserDTO> userPage = userProfileService.getActiveUsers(
                    search, 
                    currentUserId, 
                    PageRequest.of(page, size, Sort.by("updatedAt").descending())
            );

            model.addAttribute("users", userPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", userPage.getTotalPages());
            model.addAttribute("totalItems", userPage.getTotalElements());
        }

        model.addAttribute("search", search);
        model.addAttribute("resultsMessage", org.springframework.util.StringUtils.hasText(search) ? "Results for '" + search + "'" : "All Active Users");
        model.addAttribute("activeView", view);

        return "active-users";
    }

    @GetMapping("/profile/{userId}")
    public String showPublicProfile(
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        
        // If user is trying to view their own profile, redirect to private profile page
        if (userDetails != null && userDetails.getUser().getId().equals(userId)) {
            return "redirect:/profile";
        }
        
        PublicUserDTO publicProfile = userProfileService.getPublicProfile(userId);
        model.addAttribute("profile", publicProfile);
        
        return "public-profile";
    }
}
