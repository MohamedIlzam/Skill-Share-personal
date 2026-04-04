package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.exchange.ExchangeRequestResponseDTO;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.exchange.ExchangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ExchangeHistoryController {

    private final ExchangeRequestService exchangeRequestService;

    @GetMapping("/profile/exchanges")
    public String showExchangeHistory(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        List<ExchangeRequestResponseDTO> history = exchangeRequestService.getCompletedExchangesHistory(userId);
        
        model.addAttribute("exchanges", history);
        model.addAttribute("currentUserId", userId);
        
        return "exchange-history";
    }
}
