package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.dto.skill.SkillRequest;
import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.skill.SkillService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/skills")
public class SkillWebController {

    private final SkillService skillService;
    private final com.skillshare.skillshare.service.user.UserProfileService userProfileService;

    public SkillWebController(SkillService skillService, com.skillshare.skillshare.service.user.UserProfileService userProfileService) {
        this.skillService = skillService;
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public String showManageSkillsPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUser().getId();
        // Fetch User's Skills securely
        List<Skill> mySkills = skillService.getSkillsByUser(userId);
        
        // Fetch User's Profile to get mainSkillIds
        com.skillshare.skillshare.dto.user.UserProfileDTO profile = userProfileService.getProfileByUserId(userId);
        
        model.addAttribute("skills", mySkills);
        model.addAttribute("mainSkillIds", profile.getMainSkillIds());
        model.addAttribute("categories", SkillCategory.values());
        model.addAttribute("proficiencies", SkillProficiency.values());
        
        // Setup empty object for the Add Skill modal form
        if (!model.containsAttribute("newSkillRequest")) {
            model.addAttribute("newSkillRequest", new SkillRequest());
        }

        return "manage-skills";
    }

    @PostMapping("/add")
    public String addSkill(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("newSkillRequest") SkillRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.newSkillRequest", bindingResult);
            redirectAttributes.addFlashAttribute("newSkillRequest", request);
            redirectAttributes.addFlashAttribute("errorParam", "Please fix the errors in the form (name is required and max 100 chars).");
            redirectAttributes.addFlashAttribute("openAddModal", true); // Flag to re-open modal on UI
            return "redirect:/skills";
        }

        try {
            skillService.addSkill(userDetails.getUser().getId(), request);
            redirectAttributes.addFlashAttribute("successParam", "Skill successfully added!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }

        return "redirect:/skills";
    }

    @PostMapping("/{id}/edit")
    public String editSkill(
            @PathVariable("id") Long skillId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("editSkillRequest") SkillRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorParam", "Provided data was invalid. Check length and selections.");
            return "redirect:/skills";
        }

        try {
            skillService.updateSkill(userDetails.getUser().getId(), skillId, request);
            redirectAttributes.addFlashAttribute("successParam", "Skill successfully updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }

        return "redirect:/skills";
    }

    @PostMapping("/{id}/delete")
    public String deleteSkill(
            @PathVariable("id") Long skillId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            skillService.deleteSkill(userDetails.getUser().getId(), skillId);
            redirectAttributes.addFlashAttribute("successParam", "Skill successfully removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }

        return "redirect:/skills";
    }

    @PostMapping("/{id}/toggle-main")
    public String toggleMainSkill(
            @PathVariable("id") Long skillId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userProfileService.toggleMainSkill(userDetails.getUser().getId(), skillId);
            // No success message needed for a simple toggle, but we could add one
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }
        return "redirect:/skills";
    }
}
