package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.skill.SkillService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/browse")
public class BrowseSkillsController {

    private final SkillService skillService;

    public BrowseSkillsController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public String browseSkills(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(name = "q", required = false) String query,
                               Model model) {
        Long currentUserId = userDetails.getUser().getId();
        List<Skill> skills;

        // Perform search if query exists, otherwise fetch all
        if (query != null && !query.isBlank()) {
            skills = skillService.searchAvailableSkills(query, currentUserId);
            model.addAttribute("searchQuery", query);
        } else {
            skills = skillService.getAvailableSkills(currentUserId);
        }

        model.addAttribute("skills", skills);
        return "browse-skills";
    }
}
