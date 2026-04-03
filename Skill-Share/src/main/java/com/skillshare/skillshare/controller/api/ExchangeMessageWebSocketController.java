package com.skillshare.skillshare.controller.api;

import com.skillshare.skillshare.dto.exchange.MessageCreateDTO;
import com.skillshare.skillshare.dto.exchange.MessageResponseDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ExchangeMessageWebSocketController {

    private final ExchangeMessageService exchangeMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    public ExchangeMessageWebSocketController(ExchangeMessageService exchangeMessageService, SimpMessagingTemplate messagingTemplate) {
        this.exchangeMessageService = exchangeMessageService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{requestId}")
    public void processMessageFromClient(@DestinationVariable Long requestId,
                                         MessageCreateDTO messageCreateDTO,
                                         Principal principal) {
        CustomUserDetails userDetails = null;
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
            if (token.getPrincipal() instanceof CustomUserDetails) {
                userDetails = (CustomUserDetails) token.getPrincipal();
            }
        }

        if (userDetails != null) {
            Long userId = userDetails.getUser().getId();
            try {
                // Save the message and get the responseDTO broadcast-ready
                MessageResponseDTO response = exchangeMessageService.sendMessage(requestId, userId, messageCreateDTO);
                
                // Broadcast to all subscribers of this specific exchange request
                messagingTemplate.convertAndSend("/topic/exchange/" + requestId, response);
            } catch (Exception e) {
                // Log and ignore or handle error
                e.printStackTrace();
            }
        }
    }
}
