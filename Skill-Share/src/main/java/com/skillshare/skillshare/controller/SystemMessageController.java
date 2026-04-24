package com.skillshare.skillshare.controller;

import com.skillshare.skillshare.model.message.SystemMessage;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.SystemMessageRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/system-messages")
public class SystemMessageController {

    private final SystemMessageRepository systemMessageRepository;
    private final UserRepository userRepository;

    public SystemMessageController(SystemMessageRepository systemMessageRepository, UserRepository userRepository) {
        this.systemMessageRepository = systemMessageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewMessages(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<SystemMessage> messages = systemMessageRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
        
        boolean hasUnread = false;
        for (SystemMessage msg : messages) {
            if (!msg.isRead()) {
                msg.setRead(true);
                hasUnread = true;
            }
        }
        if (hasUnread) {
            systemMessageRepository.saveAll(messages);
        }

        model.addAttribute("messages", messages);

        return "system-messages";
    }
}
