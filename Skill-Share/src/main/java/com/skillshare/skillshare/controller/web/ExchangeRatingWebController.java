package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.exchange.RatingSubmitDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeRatingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ratings")
public class ExchangeRatingWebController {

    private final ExchangeRatingService exchangeRatingService;

    public ExchangeRatingWebController(ExchangeRatingService exchangeRatingService) {
        this.exchangeRatingService = exchangeRatingService;
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
}
