package com.epam.springsecuritytestapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Finds the local {@link AppUser} for an external (OAuth2) login, creating it on
 * first login. Accounts are linked by email, so callers must only pass emails
 * the provider has verified.
 */
@Service
@RequiredArgsConstructor
public class OAuth2UserProvisioner {

    private final AppUserRepository userRepository;

    @Transactional
    public AppUser findOrCreate(String verifiedEmail, String name, AuthProvider provider) {
        String email = verifiedEmail.trim().toLowerCase();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setEmail(email);
                    user.setName(StringUtils.hasText(name) ? name : email);
                    user.setRole("USER");
                    user.setProvider(provider);
                    return userRepository.save(user);
                });
    }
}
