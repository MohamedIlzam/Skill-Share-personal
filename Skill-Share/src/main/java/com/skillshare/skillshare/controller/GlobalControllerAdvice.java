package com.skillshare.skillshare.controller;

import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.repository.SystemMessageRepository;
import com.skillshare.skillshare.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final SystemMessageRepository systemMessageRepository;
    private final UserRepository userRepository;

    public GlobalControllerAdvice(SystemMessageRepository systemMessageRepository, UserRepository userRepository) {
        this.systemMessageRepository = systemMessageRepository;
        this.userRepository = userRepository;
    }

    @ModelAttribute("unreadNotificationCount")
    public Long getUnreadNotificationCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
            if (userOpt.isPresent()) {
                return systemMessageRepository.countByRecipientIdAndIsReadFalse(userOpt.get().getId());
            }
        }
        return 0L;
    }
}
