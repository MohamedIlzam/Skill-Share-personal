package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.exchange.MessageCreateDTO;
import com.skillshare.skillshare.dto.exchange.MessageResponseDTO;
import com.skillshare.skillshare.exception.ResourceNotFoundException;
import com.skillshare.skillshare.model.exchange.ExchangeRequest;
import com.skillshare.skillshare.repository.ExchangeRequestRepository;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeMessageService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/requests/{requestId}/messages")
public class ExchangeMessageWebController {

    private final ExchangeMessageService exchangeMessageService;
    private final ExchangeRequestRepository exchangeRequestRepository;

    public ExchangeMessageWebController(ExchangeMessageService exchangeMessageService, ExchangeRequestRepository exchangeRequestRepository) {
        this.exchangeMessageService = exchangeMessageService;
        this.exchangeRequestRepository = exchangeRequestRepository;
    }

    @GetMapping
    public String showMessages(@PathVariable("requestId") Long requestId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model, RedirectAttributes redirectAttributes) {
        Long userId = userDetails.getUser().getId();
        try {
            ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
            
            boolean isRequester = request.getRequester().getId().equals(userId);
            boolean isOwner = request.getSkillOwner().getId().equals(userId);
            
            if (!isRequester && !isOwner) {
                throw new AccessDeniedException("Only participants can view these messages");
            }

            List<MessageResponseDTO> messages = exchangeMessageService.getMessages(requestId, userId);
            
            model.addAttribute("messages", messages);
            model.addAttribute("request", request); // Needs some basic info for header
            model.addAttribute("currentUserId", userId);
            if (!model.containsAttribute("newMessage")) {
                model.addAttribute("newMessage", new MessageCreateDTO());
            }
            return "exchange-messages";
            
        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
            return "redirect:/requests";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", "Error loading messages");
            return "redirect:/requests";
        }
    }

    @PostMapping("/send")
    public String sendMessage(@PathVariable("requestId") Long requestId,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              @Valid @ModelAttribute("newMessage") MessageCreateDTO messageCreateDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newMessage", bindingResult);
            redirectAttributes.addFlashAttribute("newMessage", messageCreateDTO);
            return "redirect:/requests/" + requestId + "/messages";
        }

        try {
            exchangeMessageService.sendMessage(requestId, userDetails.getUser().getId(), messageCreateDTO);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }
        return "redirect:/requests/" + requestId + "/messages";
    }
}
