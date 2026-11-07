package com.epam.springsecuritytestapp.security.oauth2;

import com.epam.springsecuritytestapp.domain.AppUser;
import com.epam.springsecuritytestapp.domain.AuthProvider;
import com.epam.springsecuritytestapp.service.UserService;
import com.epam.springsecuritytestapp.security.AppUserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Used for OpenID Connect providers (Google). Spring calls it after exchanging
 * the authorization code; we map the OIDC user to our own {@link AppUser}.
 */
@Slf4j
@Service
public class AppOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OAuth2UserService<OidcUserRequest, OidcUser> delegate;
    private final UserService userService;

    @Autowired
    public AppOidcUserService(UserService userService) {
        this(new OidcUserService(), userService);
    }

    AppOidcUserService(OAuth2UserService<OidcUserRequest, OidcUser> delegate, UserService userService) {
        this.delegate = delegate;
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser oidcUser = delegate.loadUser(request);
        if (oidcUser.getEmail() == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            log.warn("OIDC login rejected: no verified email provider={} subject={}",
                    request.getClientRegistration().getRegistrationId(), oidcUser.getSubject());
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unverified_email", "The provider did not return a verified email", null));
        }
        AuthProvider provider = AuthProvider.valueOf(
                request.getClientRegistration().getRegistrationId().toUpperCase());
        AppUser user = userService.findOrCreateExternal(oidcUser.getEmail(), oidcUser.getFullName(), provider);
        return AppUserPrincipal.oidc(user, oidcUser);
    }
}
