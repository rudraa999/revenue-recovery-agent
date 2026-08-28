package com.rudra.shop.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.rudra.shop.model.User;
import com.rudra.shop.repository.UserRepository;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    private String getUsernameFromAuthentication(Authentication authentication) {
        if (authentication == null)
            return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        String username = getUsernameFromAuthentication(authentication);
        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username);
            if (user == null) {
                user = userRepository.findByEmail(username);
            }
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/account-dashboard")
    public String viewAccountDashboard(Model model, Authentication authentication) {
        String username = getUsernameFromAuthentication(authentication);
        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username);
            if (user == null) {
                user = userRepository.findByEmail(username);
            }
        }
        model.addAttribute("user", user);
        return "account-dashboard";
    }
}
