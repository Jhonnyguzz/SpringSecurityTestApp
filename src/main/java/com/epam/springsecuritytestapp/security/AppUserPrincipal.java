package com.epam.springsecuritytestapp.security;

import com.epam.springsecuritytestapp.domain.AppUser;
import com.epam.springsecuritytestapp.domain.AuthProvider;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single principal type for every login method (form, Google, GitHub), so
 * controllers can always use {@code @AuthenticationPrincipal AppUserPrincipal}.
 * {@link #getName()} and {@link #getUsername()} both return the email.
 */
@Getter
public class AppUserPrincipal implements UserDetails, OidcUser {

    private final Long id;
    private final String email;
    private final String displayName;
    private final String password;
    private final AuthProvider provider;
    private final List<GrantedAuthority> authorities;
    private final Map<String, Object> attributes;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    private AppUserPrincipal(AppUser user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.displayName = user.getName();
        this.password = user.getPassword();
        this.provider = user.getProvider();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        // Map.copyOf rejects null values, and GitHub returns several (e.g. "company", "bio").
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    public static AppUserPrincipal local(AppUser user) {
        return new AppUserPrincipal(user, Map.of(), null, null);
    }

    public static AppUserPrincipal oauth2(AppUser user, Map<String, Object> attributes) {
        return new AppUserPrincipal(user, attributes, null, null);
    }

    public static AppUserPrincipal oidc(AppUser user, OidcUser oidcUser) {
        return new AppUserPrincipal(user, oidcUser.getAttributes(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getClaims() {
        return idToken != null ? idToken.getClaims() : Map.of();
    }
}
