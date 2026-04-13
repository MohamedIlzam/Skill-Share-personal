package com.skillshare.skillshare.controller;

import com.skillshare.skillshare.model.message.SystemMessage;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.model.user.UserStatus;
import com.skillshare.skillshare.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final ExchangeRatingRepository exchangeRatingRepository;
    private final SystemMessageRepository systemMessageRepository;

    public AdminController(UserRepository userRepository,
                           SkillRepository skillRepository,
                           ExchangeRequestRepository exchangeRequestRepository,
                           ExchangeRatingRepository exchangeRatingRepository,
                           SystemMessageRepository systemMessageRepository) {
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.exchangeRatingRepository = exchangeRatingRepository;
        this.systemMessageRepository = systemMessageRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalSkills", skillRepository.count());
        model.addAttribute("totalExchanges", exchangeRequestRepository.count());
        model.addAttribute("totalRatings", exchangeRatingRepository.count());
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String query, Model model) {
        List<User> users;
        if (query != null && !query.trim().isEmpty()) {
            users = userRepository.searchByNameOrEmail(query.trim());
        } else {
            users = userRepository.findAll();
        }
        model.addAttribute("users", users);
        model.addAttribute("searchQuery", query);
        return "admin-users";
    }

    @GetMapping("/users/{id}")
    public String userProfile(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("userProfile", user.getProfile());
        model.addAttribute("targetUser", user);
        
        // Count skills logically
        long skillsCount = skillRepository.findAllByOwnerId(id).size();
        model.addAttribute("skillsCount", skillsCount);

        // Fetch ratings logic
        // This is simplified based on existing repositories. Assume we have a method or just handle it if needed.
        // Actually, we can just get average from service if we had one. Let's just pass user.

        return "admin-user-profile";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "User account deactivated successfully.");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/reactivate")
    public String reactivateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "User account reactivated successfully.");
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/message")
    public String sendMessage(@PathVariable Long id, @RequestParam String content, RedirectAttributes redirectAttributes) {
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Message content cannot be empty.");
            return "redirect:/admin/users/" + id;
        }

        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        SystemMessage message = new SystemMessage(user, content);
        systemMessageRepository.save(message);

        redirectAttributes.addFlashAttribute("successMessage", "System message sent to the user successfully.");
        return "redirect:/admin/users/" + id;
    }
}
