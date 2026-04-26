package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.user.PublicUserDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeRatingService;
import com.skillshare.skillshare.service.skill.SkillService;
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
    private final ExchangeRatingService exchangeRatingService;
    private final SkillService skillService;
    private final com.skillshare.skillshare.repository.UserRepository userRepository;

    @GetMapping("/active-users")
    public String listActiveUsers(
            com.skillshare.skillshare.dto.user.UserFilterDTO filterDTO,
            @RequestParam(value = "view", defaultValue = "users") String view,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Model model) {

        // Prevent browser caching to ensure user status and ratings are always up-to-date
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        if (filterDTO.getPage() < 0) filterDTO.setPage(0);
        if (filterDTO.getSize() <= 0) filterDTO.setSize(12);

        // Default to AVAILABLE if the form hasn't been explicitly submitted for availability
        if (request.getParameter("availability") == null) {
            filterDTO.setAvailability(com.skillshare.skillshare.model.user.AvailabilityStatus.AVAILABLE);
        } else if ("".equals(request.getParameter("availability"))) {
            filterDTO.setAvailability(null);
        }

        Long currentUserId = (userDetails != null) ? userDetails.getUser().getId() : -1L;
        
        if ("users".equals(view)) {
            Page<PublicUserDTO> userPage = userProfileService.getActiveUsers(filterDTO, currentUserId);

            model.addAttribute("users", userPage.getContent());
            model.addAttribute("currentPage", filterDTO.getPage());
            model.addAttribute("totalPages", userPage.getTotalPages());
            model.addAttribute("totalItems", userPage.getTotalElements());

            // Add top 5 rated users to the highlight section
            model.addAttribute("topRatedUsers", userProfileService.getTopRatedUsers(5));
        }

        model.addAttribute("search", filterDTO.getSearch());
        model.addAttribute("filterDTO", filterDTO);
        model.addAttribute("resultsMessage", org.springframework.util.StringUtils.hasText(filterDTO.getSearch()) ? "Results for '" + filterDTO.getSearch() + "'" : "Active Users");
        model.addAttribute("activeView", view);
        
        // Expose Skill categories for the filter dropdown
        model.addAttribute("skillCategories", com.skillshare.skillshare.model.skill.SkillCategory.values());
        
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
        
        // Prevent viewing profiles of administrators
        com.skillshare.skillshare.model.user.User targetUser = userRepository.findById(userId).orElseThrow();
        if (targetUser.getRole() == com.skillshare.skillshare.model.user.Role.ADMIN) {
             throw new com.skillshare.skillshare.exception.ResourceNotFoundException("User not found with ID: " + userId);
        }

        model.addAttribute("profile", publicProfile);
        model.addAttribute("ratingSummary", exchangeRatingService.getUserRatingSummary(userId));
        model.addAttribute("userReviews", exchangeRatingService.getUserReviews(userId));

        java.util.List<Long> mainSkillIds = userProfileService.getProfileByUserId(userId).getMainSkillIds();
        java.util.List<com.skillshare.skillshare.model.skill.Skill> availableSkills = skillService.getSkillsByUser(userId).stream()
                .filter(skill -> mainSkillIds != null && mainSkillIds.contains(skill.getId()))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("availableSkills", availableSkills);
        
        return "public-profile";
    }
}
