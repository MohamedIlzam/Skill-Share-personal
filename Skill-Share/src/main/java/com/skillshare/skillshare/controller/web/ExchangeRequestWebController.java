package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.exchange.ExchangeRequestCreateDTO;
import com.skillshare.skillshare.dto.exchange.ExchangeRequestResponseDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeRequestService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/requests")
public class ExchangeRequestWebController {

    private final ExchangeRequestService exchangeRequestService;

    public ExchangeRequestWebController(ExchangeRequestService exchangeRequestService) {
        this.exchangeRequestService = exchangeRequestService;
    }

    @GetMapping
    public String showExchangeRequests(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        List<ExchangeRequestResponseDTO> incomingRequests = exchangeRequestService.getIncomingRequests(userId);
        List<ExchangeRequestResponseDTO> outgoingRequests = exchangeRequestService.getOutgoingRequests(userId);

        model.addAttribute("incomingRequests", incomingRequests);
        model.addAttribute("outgoingRequests", outgoingRequests);

        return "exchange-requests";
    }

    @PostMapping("/create")
    public String createRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("newExchangeRequest") ExchangeRequestCreateDTO requestDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorParam", "Invalid request data. Please check your inputs.");
            return "redirect:/browse";
        }

        try {
            exchangeRequestService.createRequest(userDetails.getUser().getId(), requestDTO);
            redirectAttributes.addFlashAttribute("successParam", "Exchange request sent successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }

        return "redirect:/browse"; // Redirect back to wherever they came from, e.g., browse
    }

    @PostMapping("/{id}/accept")
    public String acceptRequest(
            @PathVariable("id") Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            exchangeRequestService.acceptRequest(requestId, userDetails.getUser().getId());
            redirectAttributes.addFlashAttribute("successParam", "Request accepted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }
        return "redirect:/requests";
    }

    @PostMapping("/{id}/reject")
    public String rejectRequest(
            @PathVariable("id") Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            exchangeRequestService.rejectRequest(requestId, userDetails.getUser().getId());
            redirectAttributes.addFlashAttribute("successParam", "Request rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }
        return "redirect:/requests";
    }

    @PostMapping("/{id}/complete")
    public String completeRequest(
            @PathVariable("id") Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            exchangeRequestService.completeRequest(requestId, userDetails.getUser().getId());
            redirectAttributes.addFlashAttribute("successParam", "Exchange marked as completed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }
        return "redirect:/requests";
    }
}
