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
import java.util.Set;
import java.util.stream.Collectors;

import com.skillshare.skillshare.model.skill.SkillCategory;
import com.skillshare.skillshare.model.skill.SkillProficiency;
import com.skillshare.skillshare.service.exchange.ExchangeRequestService;
import com.skillshare.skillshare.dto.exchange.ExchangeRequestResponseDTO;
import com.skillshare.skillshare.model.exchange.ExchangeRequestStatus;

// ... other imports

@Controller
@RequestMapping("/browse")
public class BrowseSkillsController {

    private final SkillService skillService;
    private final ExchangeRequestService exchangeRequestService;

    public BrowseSkillsController(SkillService skillService, ExchangeRequestService exchangeRequestService) {
        this.skillService = skillService;
        this.exchangeRequestService = exchangeRequestService;
    }

    @GetMapping
    public String browseSkills(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(name = "q", required = false) String query,
                               @RequestParam(name = "category", required = false) SkillCategory category,
                               @RequestParam(name = "proficiency", required = false) SkillProficiency proficiency,
                               @RequestParam(name = "sort", defaultValue = "newest") String sort,
                               Model model) {
        
        Long currentUserId = userDetails.getUser().getId();
        
        List<Skill> skills = skillService.getFilteredSkills(currentUserId, query, category, proficiency, sort);

        Set<Long> requestedSkillIds = exchangeRequestService.getOutgoingRequests(currentUserId).stream()
                .filter(req -> req.getStatus() == ExchangeRequestStatus.PENDING || req.getStatus() == ExchangeRequestStatus.ACCEPTED)
                .map(ExchangeRequestResponseDTO::getSkillId)
                .collect(Collectors.toSet());

        model.addAttribute("skills", skills);
        model.addAttribute("requestedSkillIds", requestedSkillIds);
        model.addAttribute("searchQuery", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedProficiency", proficiency);
        model.addAttribute("selectedSort", sort);
        
        // Build a descriptive results message
        StringBuilder message = new StringBuilder();
        boolean hasFilters = org.springframework.util.StringUtils.hasText(query) || category != null || proficiency != null;
        
        if (!hasFilters) {
            message.append("Explore Active Skills");
        } else {
            message.append("Results");
            if (org.springframework.util.StringUtils.hasText(query)) {
                message.append(" for '").append(query).append("'");
            }
            if (category != null) {
                message.append(" in ").append(category);
            }
            if (proficiency != null) {
                message.append(" (").append(proficiency).append(")");
            }
        }
        model.addAttribute("resultsMessage", message.toString());
        
        // Pass enum values specifically omitting 'EXPERT' from UI browsing.
        model.addAttribute("categories", SkillCategory.values());
        model.addAttribute("proficiencies", new SkillProficiency[]{
                SkillProficiency.BEGINNER, 
                SkillProficiency.INTERMEDIATE, 
                SkillProficiency.ADVANCED,
                SkillProficiency.EXPERT
        });

        return "browse-skills";
    }
}
