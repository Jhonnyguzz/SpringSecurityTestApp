package com.epam.springsecuritytestapp.config;

import com.epam.springsecuritytestapp.user.AppOAuth2UserService;
import com.epam.springsecuritytestapp.user.AppOidcUserService;
import com.epam.springsecuritytestapp.user.AppUserPrincipal;
import com.epam.springsecuritytestapp.user.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Delegating encoder: stores hashes as "{bcrypt}$2a$..." so the algorithm can
     * be changed later without invalidating existing passwords.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Tells Spring Security where to find users: DaoAuthenticationProvider calls
     * loadUserByUsername with the submitted email and then checks the password
     * with the PasswordEncoder bean. Users created via OAuth2 have no password,
     * so they are rejected here.
     */
    @Bean
    UserDetailsService userDetailsService(AppUserRepository userRepository) {
        return email -> userRepository.findByEmailIgnoreCase(email)
                .filter(user -> user.getPassword() != null)
                .map(AppUserPrincipal::local)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AppOAuth2UserService oauth2UserService,
                                            AppOidcUserService oidcUserService) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/css/**", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService)
                                .oidcUserService(oidcUserService))
                        .defaultSuccessUrl("/", true))
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
                .build();
    }
}
