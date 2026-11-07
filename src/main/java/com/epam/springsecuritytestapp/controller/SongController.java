package com.epam.springsecuritytestapp.controller;

import com.epam.springsecuritytestapp.dto.SongForm;
import com.epam.springsecuritytestapp.security.AppUserPrincipal;
import com.epam.springsecuritytestapp.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("songs", songService.findAllNewestFirst());
        return "songs";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("form", new SongForm());
        return "song-form";
    }

    @PostMapping
    public String addSong(@Valid @ModelAttribute("form") SongForm form,
                          BindingResult result,
                          @AuthenticationPrincipal AppUserPrincipal user) {
        if (result.hasErrors()) {
            return "song-form";
        }
        songService.addSong(form, user);
        return "redirect:/?added";
    }
}
