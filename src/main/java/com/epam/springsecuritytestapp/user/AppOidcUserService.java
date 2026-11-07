package com.epam.springsecuritytestapp.user;

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
@Service
public class AppOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OAuth2UserService<OidcUserRequest, OidcUser> delegate;
    private final OAuth2UserProvisioner provisioner;

    @Autowired
    public AppOidcUserService(OAuth2UserProvisioner provisioner) {
        this(new OidcUserService(), provisioner);
    }

    AppOidcUserService(OAuth2UserService<OidcUserRequest, OidcUser> delegate, OAuth2UserProvisioner provisioner) {
        this.delegate = delegate;
        this.provisioner = provisioner;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser oidcUser = delegate.loadUser(request);
        if (oidcUser.getEmail() == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unverified_email", "The provider did not return a verified email", null));
        }
        AuthProvider provider = AuthProvider.valueOf(
                request.getClientRegistration().getRegistrationId().toUpperCase());
        AppUser user = provisioner.findOrCreate(oidcUser.getEmail(), oidcUser.getFullName(), provider);
        return AppUserPrincipal.oidc(user, oidcUser);
    }
}
