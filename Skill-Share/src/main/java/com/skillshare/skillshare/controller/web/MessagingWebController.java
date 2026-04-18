package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.exchange.MessageCreateDTO;
import com.skillshare.skillshare.dto.exchange.MessageResponseDTO;
import com.skillshare.skillshare.dto.message.ConversationDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessagingWebController {

    private final ExchangeMessageService exchangeMessageService;

    @GetMapping
    public String showConversations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        List<ConversationDTO> conversations = exchangeMessageService.getUserConversations(userId);
        
        model.addAttribute("conversations", conversations);
        model.addAttribute("activeUserId", userId);
        
        return "messages";
    }

    @GetMapping("/{otherUserId}")
    public String showChat(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        
        Long userId = userDetails.getUser().getId();
        
        // Mark messages as read when opening chat
        exchangeMessageService.markMessagesAsRead(userId, otherUserId);
        
        List<ConversationDTO> conversations = exchangeMessageService.getUserConversations(userId);
        List<MessageResponseDTO> chatHistory = exchangeMessageService.getChatHistory(userId, otherUserId);
        
        // Fetch specific "Other User" for header display
        ConversationDTO activeConversation = conversations.stream()
                .filter(c -> c.getOtherUserId().equals(otherUserId))
                .findFirst()
                .orElse(null);
        
        model.addAttribute("conversations", conversations);
        model.addAttribute("chatHistory", chatHistory);
        model.addAttribute("activeConversation", activeConversation);
        model.addAttribute("targetUserId", otherUserId);
        model.addAttribute("activeUserId", userId);
        
        return "messages";
    }

    @PostMapping("/send")
    public String sendMessage(
            @RequestParam Long recipientId,
            @RequestParam String content,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        try {
            MessageCreateDTO dto = new MessageCreateDTO();
            dto.setContent(content);
            exchangeMessageService.sendDirectMessage(userDetails.getUser().getId(), recipientId, dto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send message: " + e.getMessage());
        }
        
        return "redirect:/messages/" + recipientId;
    }
}
