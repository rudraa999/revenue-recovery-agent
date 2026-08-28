package com.rudra.shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.rudra.shop.model.User;
import com.rudra.shop.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new com.rudra.shop.dto.UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.ModelAttribute("user") com.rudra.shop.dto.UserDto userDto,
            org.springframework.validation.BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        User existingUser = userService.findByEmail(userDto.getEmail());
        if (existingUser != null) {
            model.addAttribute("error", "Email already registered!");
            return "register";
        }

        User existingUsername = userService.findByUsername(userDto.getUsername());
        if (existingUsername != null) {
            model.addAttribute("error", "Username already taken!");
            return "register";
        }

        // Convert DTO to Entity
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());

        userService.registerUser(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}