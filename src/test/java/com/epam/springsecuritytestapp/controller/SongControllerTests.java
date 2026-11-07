package com.epam.springsecuritytestapp.controller;

import com.epam.springsecuritytestapp.domain.AppUser;
import com.epam.springsecuritytestapp.domain.AuthProvider;
import com.epam.springsecuritytestapp.repository.AppUserRepository;
import com.epam.springsecuritytestapp.repository.SongRepository;
import com.epam.springsecuritytestapp.security.AppUserPrincipal;
import com.epam.springsecuritytestapp.service.SongService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SongControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongService songService;

    @Test
    void anonymousUsersAreRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/songs"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void authenticatedUsersSeeAllSongs() throws Exception {
        mockMvc.perform(get("/songs").with(user(admin())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todas las canciones (7)")))
                .andExpect(content().string(containsString("Bohemian Rhapsody")));
    }

    @Test
    void onlyAdminsSeeTheDeleteButton() throws Exception {
        mockMvc.perform(get("/songs").with(user(admin())))
                .andExpect(content().string(containsString("Eliminar")));
        mockMvc.perform(get("/songs").with(user(regularUser())))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Eliminar"))));
    }

    @Test
    void adminCanDeleteSong() throws Exception {
        Long id = anySongId();

        mockMvc.perform(post("/songs/{id}/delete", id).with(user(admin())).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/songs?deleted"));

        assertThat(songRepository.findById(id)).isEmpty();
    }

    @Test
    void regularUserCannotDeleteSong() throws Exception {
        Long id = anySongId();

        mockMvc.perform(post("/songs/{id}/delete", id).with(user(regularUser())).with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(songRepository.findById(id)).isPresent();
    }

    @Test
    void deleteWithoutCsrfTokenIsRejected() throws Exception {
        Long id = anySongId();

        mockMvc.perform(post("/songs/{id}/delete", id).with(user(admin())))
                .andExpect(status().isForbidden());

        assertThat(songRepository.findById(id)).isPresent();
    }

    @Test
    @WithMockUser(roles = "USER")
    void serviceRejectsNonAdminEvenWhenCalledDirectly() {
        Long id = anySongId();

        assertThatThrownBy(() -> songService.deleteSong(id, regularUser()))
                .isInstanceOf(AuthorizationDeniedException.class);
        assertThat(songRepository.findById(id)).isPresent();
    }

    private Long anySongId() {
        return songRepository.findAllByOrderByCreatedAtDesc().getFirst().getId();
    }

    private AppUserPrincipal admin() {
        return AppUserPrincipal.local(userRepository.findByEmailIgnoreCase("admin@example.com").orElseThrow());
    }

    private static AppUserPrincipal regularUser() {
        AppUser user = new AppUser();
        user.setEmail("user@example.com");
        user.setName("User");
        user.setPassword("{noop}irrelevant");
        user.setRole("USER");
        user.setProvider(AuthProvider.LOCAL);
        return AppUserPrincipal.local(user);
    }
}
