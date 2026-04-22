package com.skillshare.skillshare.controller.web;

import com.skillshare.skillshare.model.skill.Skill;
import com.skillshare.skillshare.security.CustomUserDetails;
import com.skillshare.skillshare.service.skill.SkillBookmarkService;
import com.skillshare.skillshare.service.skill.SkillService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final SkillBookmarkService skillBookmarkService;

    public BrowseSkillsController(SkillService skillService,
                                  ExchangeRequestService exchangeRequestService,
                                  SkillBookmarkService skillBookmarkService) {
        this.skillService = skillService;
        this.exchangeRequestService = exchangeRequestService;
        this.skillBookmarkService = skillBookmarkService;
    }

    @GetMapping
    public String browseSkills(@AuthenticationPrincipal com.skillshare.skillshare.security.CustomUserDetails userDetails,
                               @RequestParam(name = "view", defaultValue = "available") String view,
                               @RequestParam(name = "q", required = false) String query,
                               @RequestParam(name = "category", required = false) com.skillshare.skillshare.model.skill.SkillCategory category,
                               @RequestParam(name = "proficiency", required = false) com.skillshare.skillshare.model.skill.SkillProficiency proficiency,
                               @RequestParam(name = "sort", defaultValue = "newest") String sort,
                               jakarta.servlet.http.HttpServletResponse response,
                               Model model) {
        
        // Prevent browser caching to ensure bookmark icons are always up-to-date
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        Long currentUserId = userDetails.getUser().getId();

        Set<Long> bookmarkedSkillIds = skillBookmarkService.getBookmarkedSkillIds(currentUserId);

        boolean onlyAvailableOwners = !"all".equalsIgnoreCase(view);
        List<Skill> skills = skillService.getFilteredSkills(currentUserId, query, category, proficiency, sort, onlyAvailableOwners);

        if ("available".equalsIgnoreCase(view)) {
            skills = skills.stream().filter(Skill::isMainSkill).collect(java.util.stream.Collectors.toList());
        } else if ("bookmarked".equalsIgnoreCase(view)) {
            skills = skills.stream()
                    .filter(s -> bookmarkedSkillIds.contains(s.getId()))
                    .collect(java.util.stream.Collectors.toList());
        }

        Set<Long> requestedSkillIds = exchangeRequestService.getOutgoingRequests(currentUserId).stream()
                .filter(req -> req.getStatus() == ExchangeRequestStatus.PENDING || req.getStatus() == ExchangeRequestStatus.ACCEPTED)
                .map(ExchangeRequestResponseDTO::getSkillId)
                .collect(Collectors.toSet());

        model.addAttribute("skills", skills);
        model.addAttribute("requestedSkillIds", requestedSkillIds);
        model.addAttribute("bookmarkedSkillIds", bookmarkedSkillIds);
        model.addAttribute("selectedView", view);
        model.addAttribute("searchQuery", query);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedProficiency", proficiency);
        model.addAttribute("selectedSort", sort);
        
        // Build a descriptive results message
        StringBuilder message = new StringBuilder();
        boolean hasFilters = org.springframework.util.StringUtils.hasText(query) || category != null || proficiency != null;
        
        if (!hasFilters) {
            if ("all".equalsIgnoreCase(view)) {
                message.append("Explore All Skills");
            } else if ("bookmarked".equalsIgnoreCase(view)) {
                message.append("Your Bookmarked Skills");
            } else {
                message.append("Explore Available Skills");
            }
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

    @PostMapping("/bookmark")
    public String toggleBookmark(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam("skillId") Long skillId,
                                 @RequestParam(name = "view", defaultValue = "available") String view,
                                 @RequestParam(name = "q", required = false) String query,
                                 @RequestParam(name = "category", required = false) SkillCategory category,
                                 @RequestParam(name = "proficiency", required = false) SkillProficiency proficiency,
                                 @RequestParam(name = "sort", defaultValue = "newest") String sort,
                                 RedirectAttributes redirectAttributes) {

        try {
            boolean nowBookmarked = skillBookmarkService.toggleBookmark(userDetails.getUser().getId(), skillId);
            redirectAttributes.addFlashAttribute("successParam", nowBookmarked ? "Skill bookmarked." : "Bookmark removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorParam", e.getMessage());
        }

        org.springframework.web.util.UriComponentsBuilder builder = org.springframework.web.util.UriComponentsBuilder.fromPath("/browse")
                .queryParam("view", view)
                .queryParam("sort", sort);
        if (org.springframework.util.StringUtils.hasText(query)) builder.queryParam("q", query);
        if (category != null) builder.queryParam("category", category);
        if (proficiency != null) builder.queryParam("proficiency", proficiency);

        return "redirect:" + builder.toUriString();
    }
}
