package com.epam.springsecuritytestapp.web;

import com.epam.springsecuritytestapp.user.RegistrationForm;
import com.epam.springsecuritytestapp.user.RegistrationService;
import com.epam.springsecuritytestapp.user.RegistrationService.EmailAlreadyRegisteredException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @GetMapping
    public String form(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            registrationService.register(form);
        } catch (EmailAlreadyRegisteredException e) {
            result.rejectValue("email", "duplicate", "Ya existe una cuenta con este correo");
            return "register";
        }
        return "redirect:/login?registered";
    }
}
