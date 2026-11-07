package com.epam.springsecuritytestapp.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Audit log of Spring Security events. Spring Security publishes them as regular
 * application events (ProviderManager, LogoutFilter, AuthorizationFilter); this
 * class only listens. Credentials are never logged.
 */
@Slf4j
@Component
public class SecurityEventLogger {

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        log.info("Login success user={} method={}", auth.getName(), auth.getClass().getSimpleName());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        log.warn("Login failure user={} reason={}: {}",
                event.getAuthentication().getName(),
                event.getException().getClass().getSimpleName(),
                event.getException().getMessage());
    }

    @EventListener
    public void onLogout(LogoutSuccessEvent event) {
        log.info("Logout user={}", event.getAuthentication().getName());
    }

    @EventListener
    public void onAccessDenied(AuthorizationDeniedEvent<?> event) {
        Authentication auth = event.getAuthentication().get();
        String target = event.getSource() instanceof HttpServletRequest request
                ? request.getMethod() + " " + request.getRequestURI()
                : String.valueOf(event.getSource());
        log.info("Access denied user={} target={}", auth.getName(), target);
    }
}
