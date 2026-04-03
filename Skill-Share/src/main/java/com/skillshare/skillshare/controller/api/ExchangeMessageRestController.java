package com.skillshare.skillshare.controller.api;

import com.skillshare.skillshare.dto.exchange.MessageCreateDTO;
import com.skillshare.skillshare.dto.exchange.MessageResponseDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeMessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests/{requestId}/messages")
public class ExchangeMessageRestController {

    private final ExchangeMessageService exchangeMessageService;

    public ExchangeMessageRestController(ExchangeMessageService exchangeMessageService) {
        this.exchangeMessageService = exchangeMessageService;
    }

    @GetMapping
    public ResponseEntity<List<MessageResponseDTO>> getMessages(
            @PathVariable("requestId") Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUser().getId();
            List<MessageResponseDTO> messages = exchangeMessageService.getMessages(requestId, userId);
            return ResponseEntity.ok(messages);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(
            @PathVariable("requestId") Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MessageCreateDTO messageCreateDTO) {
        try {
            Long userId = userDetails.getUser().getId();
            MessageResponseDTO response = exchangeMessageService.sendMessage(requestId, userId, messageCreateDTO);
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
