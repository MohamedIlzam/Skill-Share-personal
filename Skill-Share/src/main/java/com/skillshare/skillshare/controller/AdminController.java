package com.skillshare.skillshare.controller;

import com.skillshare.skillshare.model.message.SystemMessage;
import com.skillshare.skillshare.model.user.User;
import com.skillshare.skillshare.model.user.UserStatus;
import com.skillshare.skillshare.model.user.Role;
import com.skillshare.skillshare.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import com.skillshare.skillshare.model.skill.Skill;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
        model.addAttribute("totalUsers", userRepository.countByRole(Role.USER));
        model.addAttribute("totalSkills", skillRepository.count());
        model.addAttribute("totalExchanges", exchangeRequestRepository.count());
        model.addAttribute("totalRatings", exchangeRatingRepository.count());
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String query, Model model) {
        List<User> users;
        if (query != null && !query.trim().isEmpty()) {
            users = userRepository.findAllByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query.trim(), query.trim());
        } else {
            users = userRepository.findAll();
        }
        
        // Filter out administrators so they do not show up on the admin pages
        users = users.stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("users", users);
        model.addAttribute("searchQuery", query);
        return "admin-users";
    }

    @GetMapping("/users/{id}")
    public String userProfile(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("userProfile", user.getProfile());
        model.addAttribute("targetUser", user);
        
        List<Skill> userSkills = skillRepository.findAllByOwnerId(id);
        model.addAttribute("skillsCount", userSkills.size());
        model.addAttribute("userSkills", userSkills);

        Long totalRatings = exchangeRatingRepository.countRatingsForUser(id);
        model.addAttribute("totalRatings", totalRatings != null ? totalRatings : 0L);

        Double averageRating = exchangeRatingRepository.getAverageRatingForUser(id);
        model.addAttribute("averageRating", averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0);

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
