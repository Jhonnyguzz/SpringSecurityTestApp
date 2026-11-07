package com.epam.springsecuritytestapp.service;

import com.epam.springsecuritytestapp.domain.AppUser;
import com.epam.springsecuritytestapp.domain.AuthProvider;
import com.epam.springsecuritytestapp.dto.RegistrationForm;
import com.epam.springsecuritytestapp.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Single place where {@link AppUser}s are created, whatever the login method:
 * form registration (LOCAL) or first OAuth2 login (GOOGLE / GITHUB).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_ROLE = "USER";

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AppUser registerLocal(RegistrationForm form) {
        String email = normalizeEmail(form.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        return create(email, form.getName(), passwordEncoder.encode(form.getPassword()), AuthProvider.LOCAL);
    }

    /**
     * Finds the user for an external login, creating it on first login. Accounts are
     * linked by email, so callers must only pass emails the provider has verified.
     */
    @Transactional
    public AppUser findOrCreateExternal(String verifiedEmail, String name, AuthProvider provider) {
        String email = normalizeEmail(verifiedEmail);
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> create(email, name, null, provider));
    }

    private AppUser create(String email, String name, String encodedPassword, AuthProvider provider) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setName(StringUtils.hasText(name) ? name.trim() : email);
        user.setPassword(encodedPassword);
        user.setRole(DEFAULT_ROLE);
        user.setProvider(provider);
        return userRepository.save(user);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException(String email) {
            super("Email already registered: " + email);
        }
    }
}
