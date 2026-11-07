package com.epam.springsecuritytestapp.security;

import com.epam.springsecuritytestapp.repository.AppUserRepository;
import com.epam.springsecuritytestapp.security.oauth2.AppOAuth2UserService;
import com.epam.springsecuritytestapp.security.oauth2.AppOidcUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Form-login counterpart of {@link AppOAuth2UserService} / {@link AppOidcUserService}:
 * DaoAuthenticationProvider calls loadUserByUsername with the submitted email and then
 * checks the password with the PasswordEncoder bean. Users created via OAuth2 have no
 * password, so they are rejected here.
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public JpaUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .filter(user -> user.getPassword() != null)
                .map(AppUserPrincipal::local)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
