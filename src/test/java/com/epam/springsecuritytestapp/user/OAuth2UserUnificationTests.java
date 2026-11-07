package com.epam.springsecuritytestapp.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@Transactional
class OAuth2UserUnificationTests {

    @Autowired
    private OAuth2UserProvisioner provisioner;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    private final OAuth2AccessToken accessToken = new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, "token-123", Instant.now(), Instant.now().plusSeconds(60));

    private MockRestServiceServer githubApi;
    private AppOAuth2UserService githubService;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        githubApi = MockRestServiceServer.bindTo(builder).build();
        OAuth2User githubUser = new DefaultOAuth2User(List.of(),
                Map.of("id", 42, "login", "octocat", "name", "The Octocat"), "id");
        githubService = new AppOAuth2UserService(request -> githubUser, provisioner, builder.build());
    }

    @Test
    void githubLoginCreatesUserWithPrimaryVerifiedEmail() {
        respondWithGithubEmails("""
                [{"email":"other@example.com","primary":false,"verified":true},
                 {"email":"Octo@Example.com","primary":true,"verified":true}]""");

        AppUserPrincipal principal = (AppUserPrincipal) githubService.loadUser(githubRequest());

        assertThat(principal.getName()).isEqualTo("octo@example.com");
        assertThat(principal.getDisplayName()).isEqualTo("The Octocat");
        assertThat(principal.getProvider()).isEqualTo(AuthProvider.GITHUB);
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        assertThat(principal.<String>getAttribute("login")).isEqualTo("octocat");
        assertThat(userRepository.findByEmailIgnoreCase("octo@example.com")).isPresent();
        githubApi.verify();
    }

    @Test
    void githubLoginLinksToExistingLocalAccount() {
        respondWithGithubEmails("""
                [{"email":"admin@example.com","primary":true,"verified":true}]""");
        long usersBefore = userRepository.count();

        AppUserPrincipal principal = (AppUserPrincipal) githubService.loadUser(githubRequest());

        assertThat(principal.getDisplayName()).isEqualTo("Admin");
        assertThat(principal.getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
        assertThat(userRepository.count()).isEqualTo(usersBefore);
    }

    @Test
    void githubLoginFailsWithoutPrimaryVerifiedEmail() {
        respondWithGithubEmails("""
                [{"email":"octo@example.com","primary":true,"verified":false}]""");

        assertThatThrownBy(() -> githubService.loadUser(githubRequest()))
                .isInstanceOf(OAuth2AuthenticationException.class);
        assertThat(userRepository.findByEmailIgnoreCase("octo@example.com")).isEmpty();
    }

    @Test
    void googleLoginCreatesUserFromVerifiedEmail() {
        AppUserPrincipal principal = (AppUserPrincipal) googleService(true).loadUser(googleRequest(true));

        assertThat(principal.getName()).isEqualTo("ana@gmail.com");
        assertThat(principal.getDisplayName()).isEqualTo("Ana Gómez");
        assertThat(principal.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(principal.getIdToken()).isNotNull();
    }

    @Test
    void googleLoginRejectsUnverifiedEmail() {
        assertThatThrownBy(() -> googleService(false).loadUser(googleRequest(false)))
                .isInstanceOf(OAuth2AuthenticationException.class);
        assertThat(userRepository.findByEmailIgnoreCase("ana@gmail.com")).isEmpty();
    }

    @Test
    void oauth2UsersCannotUseFormLogin() {
        provisioner.findOrCreate("ana@gmail.com", "Ana", AuthProvider.GOOGLE);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ana@gmail.com"))
                .isInstanceOf(UsernameNotFoundException.class);
        assertThat(userDetailsService.loadUserByUsername("admin@example.com"))
                .isInstanceOf(AppUserPrincipal.class);
    }

    private void respondWithGithubEmails(String json) {
        githubApi.expect(requestTo("https://api.github.com/user/emails"))
                .andExpect(header("Authorization", "Bearer token-123"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private OAuth2UserRequest githubRequest() {
        ClientRegistration registration = CommonOAuth2Provider.GITHUB.getBuilder("github")
                .clientId("id").clientSecret("secret").build();
        return new OAuth2UserRequest(registration, accessToken);
    }

    private AppOidcUserService googleService(boolean emailVerified) {
        return new AppOidcUserService(request -> new DefaultOidcUser(List.of(), idToken(emailVerified)), provisioner);
    }

    private OidcUserRequest googleRequest(boolean emailVerified) {
        ClientRegistration registration = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId("id").clientSecret("secret").build();
        return new OidcUserRequest(registration, accessToken, idToken(emailVerified));
    }

    private OidcIdToken idToken(boolean emailVerified) {
        return OidcIdToken.withTokenValue("id-token")
                .subject("google-sub-1")
                .claim("email", "ana@gmail.com")
                .claim("email_verified", emailVerified)
                .claim("name", "Ana Gómez")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }
}
