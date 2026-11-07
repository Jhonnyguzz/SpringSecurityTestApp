package com.epam.springsecuritytestapp.service;

import com.epam.springsecuritytestapp.domain.AppUser;
import com.epam.springsecuritytestapp.domain.AuthProvider;
import com.epam.springsecuritytestapp.dto.RegistrationForm;
import com.epam.springsecuritytestapp.service.UserService.EmailAlreadyRegisteredException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registerLocalCreatesUserWithEncodedPassword() {
        AppUser user = userService.registerLocal(form(" Ana ", " Ana@Example.com ", "secret123"));

        assertThat(user.getEmail()).isEqualTo("ana@example.com");
        assertThat(user.getName()).isEqualTo("Ana");
        assertThat(user.getRole()).isEqualTo("USER");
        assertThat(user.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(passwordEncoder.matches("secret123", user.getPassword())).isTrue();
    }

    @Test
    void registerLocalRejectsEmailAlreadyUsedByExternalAccount() {
        userService.findOrCreateExternal("ana@gmail.com", "Ana", AuthProvider.GOOGLE);

        assertThatThrownBy(() -> userService.registerLocal(form("Ana", "ANA@gmail.com", "secret123")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void findOrCreateExternalCreatesUserWithoutPasswordOnlyOnce() {
        AppUser first = userService.findOrCreateExternal("Octo@Example.com", null, AuthProvider.GITHUB);
        AppUser second = userService.findOrCreateExternal("octo@example.com", "Octo", AuthProvider.GITHUB);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(first.getName()).isEqualTo("octo@example.com");
        assertThat(first.getPassword()).isNull();
        assertThat(first.getProvider()).isEqualTo(AuthProvider.GITHUB);
    }

    private static RegistrationForm form(String name, String email, String password) {
        RegistrationForm form = new RegistrationForm();
        form.setName(name);
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(password);
        return form;
    }
}
