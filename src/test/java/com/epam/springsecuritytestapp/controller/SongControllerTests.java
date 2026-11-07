package com.epam.springsecuritytestapp.controller;

import com.epam.springsecuritytestapp.repository.AppUserRepository;
import com.epam.springsecuritytestapp.security.AppUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SongControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void anonymousUsersAreRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/songs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void authenticatedUsersSeeAllSongs() throws Exception {
        AppUserPrincipal admin = AppUserPrincipal.local(userRepository.findByEmailIgnoreCase("admin@example.com").orElseThrow());

        mockMvc.perform(get("/songs").with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todas las canciones (7)")))
                .andExpect(content().string(containsString("Bohemian Rhapsody")));
    }
}
