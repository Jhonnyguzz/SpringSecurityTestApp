package com.epam.springsecuritytestapp.controller;

import com.epam.springsecuritytestapp.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SongRepository songRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("latestSongs", songRepository.findTop5ByOrderByCreatedAtDesc());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
