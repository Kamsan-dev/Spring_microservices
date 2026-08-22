package com.kamsan.discoveryservice.security.config;

import com.kamsan.discoveryservice.security.authentication.client.ClientAuthenticationProvider;
import com.kamsan.discoveryservice.security.authentication.client.ClientRefreshTokenAuthenticationConverter;
import com.kamsan.discoveryservice.security.authentication.mfa.MfaAuthenticationHandler;
import com.kamsan.discoveryservice.security.token.ClientOAuth2RefreshTokenGenerator;
import com.kamsan.discoveryservice.security.token.UserJwtGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.hc.core5.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.oauth2.server.authorization.OAuth2TokenType.ACCESS_TOKEN;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {
    private final JwtConfiguration jwtConfiguration;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServiceConfig(HttpSecurity http, RegisteredClientRepository registeredClientRepository) {
        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));

        return http.oauth2AuthorizationServer(authorizationServer -> {
            http.securityMatcher(authorizationServer.getEndpointsMatcher())
                .with(authorizationServer, Customizer.withDefaults())
                .exceptionHandling(e -> e.accessDeniedPage("/accessdenied")
                                         .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
            authorizationServer
                    .tokenGenerator(tokenGenerator())
                    .clientAuthentication(authentication -> {
                        authentication.authenticationConverter(new ClientRefreshTokenAuthenticationConverter());
                        authentication.authenticationProvider(new ClientAuthenticationProvider(registeredClientRepository));
                    }).oidc(Customizer.withDefaults());
        }).build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/login").permitAll()
                .requestMatchers(POST, "/logout").permitAll()
                .requestMatchers("/mfa").hasAuthority("MFA_REQUIRED")
                .anyRequest().authenticated());
        http.formLogin(login -> login
                .loginPage("/login")
                .successHandler(new MfaAuthenticationHandler("/mfa", "MFA_REQUIRED"))
                .failureHandler(new SimpleUrlAuthenticationFailureHandler("/login?error")));
        http.logout(logout -> logout.logoutSuccessUrl("http://localhost:3000")
                                    .addLogoutHandler(new CookieClearingLogoutHandler("JSESSIONID")));
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(List.of("http://securedoc.com", "http://localhost:3000", "http://localhost:4200", "http://localhost:4200", "100.14.214.212:3000", "http://96.255.228.129:3000", "http://localhost:4200", "http://localhost:4200", "http://localhost:3000", "http://securecapita.org", "http://192.168.1.216:3000"));
        corsConfiguration.setAllowedHeaders(Arrays.asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, "X_REQUESTED_WITH", ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_CREDENTIALS));
        corsConfiguration.setExposedHeaders(Arrays.asList(ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION, "X_REQUESTED_WITH", ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_ALLOW_CREDENTIALS));
        corsConfiguration.setAllowedMethods(Arrays.asList(GET.name(), POST.name(), PUT.name(), PATCH.name(), DELETE.name(), OPTIONS.name()));
        corsConfiguration.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/", corsConfiguration);
        return source;
    }

    /**
     * Ce bean indique à Spring Authorization Server comment générer les différents tokens OAuth2, notamment l'access-token  et le refresh-token.
     * DelegatingOAuth2TokenGenerator va regarder quel type de token Spring lui demande et déléguer la génération au composant approprié.
     *
     * @return
     */
    @Bean
    public OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator() {
        var jwtGenerator = UserJwtGenerator.init((new NimbusJwtEncoder(jwtConfiguration.jwkSource())));
        /*
        setJwtCustomizer est appelé pour personnaliser l'access-token et ajouter des claims nécessaires.
         */
        jwtGenerator.setJwtCustomizer(customizer());
        OAuth2TokenGenerator<OAuth2RefreshToken> oAuth2RefreshTokenOAuth2TokenGenerator = new ClientOAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(jwtGenerator, oAuth2RefreshTokenOAuth2TokenGenerator);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> customizer() {
        return context -> {
            if (ACCESS_TOKEN.equals(context.getTokenType())) {
                context.getClaims().claims(claims -> claims.put("authorities", getAuthorities(context)));
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new SavedRequestAwareAuthenticationSuccessHandler();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    private String getAuthorities(JwtEncodingContext context) {
        return context.getPrincipal()
                      .getAuthorities()
                      .stream()
                      .map(GrantedAuthority::getAuthority)
                      .collect(Collectors.joining(","));
    }
}
