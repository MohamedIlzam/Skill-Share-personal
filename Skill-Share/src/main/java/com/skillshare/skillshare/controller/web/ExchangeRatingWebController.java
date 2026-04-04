package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.exchange.RatingResponseDTO;
import com.skillshare.skillshare.dto.exchange.RatingSubmitDTO;
import com.skillshare.skillshare.dto.user.PublicUserDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeRatingService;
import com.skillshare.skillshare.service.user.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ratings")
public class ExchangeRatingWebController {

    private final ExchangeRatingService exchangeRatingService;
    private final UserProfileService userProfileService;

    public ExchangeRatingWebController(ExchangeRatingService exchangeRatingService, UserProfileService userProfileService) {
        this.exchangeRatingService = exchangeRatingService;
        this.userProfileService = userProfileService;
    }

    @PostMapping("/submit")
    public String submitRating(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("ratingDto") RatingSubmitDTO ratingDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorParam", "Invalid rating data. Please select a score between 1 and 5.");
            return "redirect:/requests";
        }

        try {
            exchangeRatingService.submitRating(userDetails.getUser().getId(), ratingDto);
            redirectAttributes.addFlashAttribute("successParam", "Thank you! Your feedback has been submitted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }

        return "redirect:/requests";
    }

    @GetMapping("/user/{userId}")
    public String showUserReviews(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        PublicUserDTO publicProfile = userProfileService.getPublicProfile(userId);
        Page<RatingResponseDTO> reviewsPage = exchangeRatingService.getPaginatedUserReviews(userId, page, size);

        model.addAttribute("profile", publicProfile);
        model.addAttribute("reviewsPage", reviewsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewsPage.getTotalPages());
        model.addAttribute("totalItems", reviewsPage.getTotalElements());
        
        return "user-reviews";
    }
}
