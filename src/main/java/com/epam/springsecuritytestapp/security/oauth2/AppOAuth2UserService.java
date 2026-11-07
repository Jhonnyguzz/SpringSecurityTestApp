package com.epam.springsecuritytestapp.security.oauth2;

import com.epam.springsecuritytestapp.domain.AppUser;
import com.epam.springsecuritytestapp.domain.AuthProvider;
import com.epam.springsecuritytestapp.service.UserService;
import com.epam.springsecuritytestapp.security.AppUserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Used for plain OAuth2 providers (GitHub). GitHub's user-info may not include
 * the email (private emails), so we ask /user/emails for the primary verified one.
 */
@Service
public class AppOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String GITHUB_EMAILS_URL = "https://api.github.com/user/emails";

    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;
    private final UserService userService;
    private final RestClient restClient;

    @Autowired
    public AppOAuth2UserService(UserService userService) {
        this(new DefaultOAuth2UserService(), userService, RestClient.create());
    }

    AppOAuth2UserService(OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate,
                         UserService userService,
                         RestClient restClient) {
        this.delegate = delegate;
        this.userService = userService;
        this.restClient = restClient;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oauth2User = delegate.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();
        if (!"github".equals(registrationId)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider", "Unsupported provider: " + registrationId, null));
        }

        String email = fetchGithubPrimaryVerifiedEmail(request.getAccessToken().getTokenValue());
        String name = oauth2User.getAttribute("name");
        if (name == null) {
            name = oauth2User.getAttribute("login");
        }
        AppUser user = userService.findOrCreateExternal(email, name, AuthProvider.GITHUB);
        return AppUserPrincipal.oauth2(user, oauth2User.getAttributes());
    }

    private String fetchGithubPrimaryVerifiedEmail(String accessToken) {
        List<GithubEmail> emails = restClient.get()
                .uri(GITHUB_EMAILS_URL)
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return (emails == null ? List.<GithubEmail>of() : emails).stream()
                .filter(e -> e.primary() && e.verified())
                .map(GithubEmail::email)
                .findFirst()
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        new OAuth2Error("unverified_email", "GitHub account has no primary verified email", null)));
    }

    record GithubEmail(String email, boolean primary, boolean verified) {
    }
}
