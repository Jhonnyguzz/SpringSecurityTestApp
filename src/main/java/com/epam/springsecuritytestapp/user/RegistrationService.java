package com.epam.springsecuritytestapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AppUser register(RegistrationForm form) {
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new EmailAlreadyRegisteredException(form.getEmail());
        }
        AppUser user = new AppUser();
        user.setName(form.getName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole("USER");
        user.setProvider(AuthProvider.LOCAL);
        return userRepository.save(user);
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException(String email) {
            super("Email already registered: " + email);
        }
    }
}
