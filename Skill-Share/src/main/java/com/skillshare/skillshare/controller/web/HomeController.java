package com.skillshare.skillshare.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(java.security.Principal principal) {
        if (principal != null) {
            return "redirect:/active-users";
        }
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
