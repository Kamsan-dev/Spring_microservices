package com.kamsan.authorizationserver.security.token;

import com.kamsan.authorizationserver.model.User;
import com.kamsan.authorizationserver.utils.UserUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

public class UserJwtGenerator implements OAuth2TokenGenerator<Jwt> {
    private final JwtEncoder jwtEncoder;
    private @Nullable OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer;
    private Clock clock = Clock.systemUTC();

    public UserJwtGenerator(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    @Nullable
    public Jwt generate(OAuth2TokenContext context) {
        if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) && !OidcParameterNames.ID_TOKEN.equals(context.getTokenType()
                                                                                                                       .getValue())) {
            return null;
        } else if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) && !OAuth2TokenFormat.SELF_CONTAINED.equals(
                context.getRegisteredClient()
                       .getTokenSettings()
                       .getAccessTokenFormat())) {
            return null;
        } else {
            RegisteredClient registeredClient = context.getRegisteredClient();
            String issuer = context.getAuthorizationServerContext().getIssuer();
            Instant issuedAt = this.clock.instant();
            JwsAlgorithm jwsAlgorithm = SignatureAlgorithm.RS256;
            Instant expiresAt;
            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                expiresAt = issuedAt.plus(30L, ChronoUnit.MINUTES);
                jwsAlgorithm = registeredClient.getTokenSettings().getIdTokenSignatureAlgorithm();
            } else {
                expiresAt = issuedAt.plus(registeredClient.getTokenSettings().getAccessTokenTimeToLive());
            }

            Authentication principal = context.getPrincipal();
            User user = UserUtils.getUser(principal);
            Assert.notNull(principal, "principal cannot be null");
            AuthorizationGrantType authorizationGrantType = context.getAuthorizationGrantType();
            Assert.notNull(authorizationGrantType, "authorizationGrantType cannot be null");
            JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                                                             .issuer(issuer)
                                                             .subject(user.getUserPublicId().toString())
                                                             .audience(Collections.singletonList(registeredClient.getClientId()))
                                                             .issuedAt(issuedAt)
                                                             .expiresAt(expiresAt)
                                                             .id(UUID.randomUUID().toString());

            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                claimsBuilder.notBefore(issuedAt);
                if (!CollectionUtils.isEmpty(context.getAuthorizedScopes())) {
                    claimsBuilder.claim("scope", context.getAuthorizedScopes());
                }
            } else if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType().getValue())) {
                claimsBuilder.claim("azp", registeredClient.getClientId());
                if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationGrantType)) {
                    OAuth2Authorization authorization = context.getAuthorization();
                    Assert.notNull(authorization, "authorization cannot be null");
                    OAuth2AuthorizationRequest authorizationRequest = (OAuth2AuthorizationRequest) authorization.getAttribute(
                            OAuth2AuthorizationRequest.class.getName());
                    Assert.notNull(authorizationRequest, "authorizationRequest cannot be null");
                    String nonce = (String) authorizationRequest.getAdditionalParameters().get("nonce");
                    if (StringUtils.hasText(nonce)) {
                        claimsBuilder.claim("nonce", nonce);
                    }

                    SessionInformation sessionInformation = (SessionInformation) context.get(SessionInformation.class);
                    if (sessionInformation != null) {
                        claimsBuilder.claim("sid", sessionInformation.getSessionId());
                        claimsBuilder.claim("auth_time", getAuthenticationTime(principal));
                    }
                } else if (AuthorizationGrantType.REFRESH_TOKEN.equals(authorizationGrantType)) {
                    OAuth2Authorization authorization = context.getAuthorization();
                    Assert.notNull(authorization, "authorization cannot be null");
                    OAuth2Authorization.Token<OidcIdToken> authorizedIdToken = authorization.getToken(OidcIdToken.class);
                    Assert.notNull(authorizedIdToken, "authorizedIdToken cannot be null");
                    OidcIdToken currentIdToken = (OidcIdToken) authorizedIdToken.getToken();
                    String sidClaim = (String) currentIdToken.getClaim("sid");
                    if (sidClaim != null) {
                        claimsBuilder.claim("sid", sidClaim);
                    }

                    Date authTimeClaim = (Date) currentIdToken.getClaim("auth_time");
                    if (authTimeClaim != null) {
                        claimsBuilder.claim("auth_time", authTimeClaim);
                    }
                }
            }

            JwsHeader.Builder jwsHeaderBuilder = JwsHeader.with(jwsAlgorithm);
            if (this.jwtCustomizer != null) {
                JwtEncodingContext.Builder jwtContextBuilder = JwtEncodingContext.with(jwsHeaderBuilder, claimsBuilder)
                                                                                 .registeredClient(context.getRegisteredClient())
                                                                                 .principal(principal)
                                                                                 .authorizationServerContext(context.getAuthorizationServerContext())
                                                                                 .authorizedScopes(context.getAuthorizedScopes())
                                                                                 .tokenType(context.getTokenType())
                                                                                 .authorizationGrantType(
                                                                                         authorizationGrantType);
                if (context.getAuthorization() != null) {
                    jwtContextBuilder.authorization(context.getAuthorization());
                }

                if (context.getAuthorizationGrant() != null) {
                    jwtContextBuilder.authorizationGrant(context.getAuthorizationGrant());
                }

                if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType().getValue())) {
                    SessionInformation sessionInformation = (SessionInformation) context.get(SessionInformation.class);
                    if (sessionInformation != null) {
                        jwtContextBuilder.put(SessionInformation.class, sessionInformation);
                    }
                }

                if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                    Jwt dPoPProofJwt = (Jwt) context.get(OAuth2TokenContext.DPOP_PROOF_KEY);
                    if (dPoPProofJwt != null) {
                        jwtContextBuilder.put(OAuth2TokenContext.DPOP_PROOF_KEY, dPoPProofJwt);
                    }
                }

                JwtEncodingContext jwtContext = jwtContextBuilder.build();
                this.jwtCustomizer.customize(jwtContext);
            }

            JwsHeader jwsHeader = jwsHeaderBuilder.build();
            JwtClaimsSet claims = claimsBuilder.build();
            return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));
        }
    }

    public void setJwtCustomizer(OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
        Assert.notNull(jwtCustomizer, "jwtCustomizer cannot be null");
        this.jwtCustomizer = jwtCustomizer;
    }

    public void setClock(Clock clock) {
        Assert.notNull(clock, "clock cannot be null");
        this.clock = clock;
    }

    static Date getAuthenticationTime(Authentication authentication) {
        Instant authenticationTime = null;

        for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
            if (grantedAuthority instanceof FactorGrantedAuthority factorGrantedAuthority) {
                if (authenticationTime == null || factorGrantedAuthority.getIssuedAt().isAfter(authenticationTime)) {
                    authenticationTime = factorGrantedAuthority.getIssuedAt();
                }
            }
        }

        Assert.notNull(authenticationTime, "authenticationTime cannot be null");
        return Date.from(authenticationTime);
    }

    public static UserJwtGenerator init(JwtEncoder jwtEncoder) {
        return new UserJwtGenerator(jwtEncoder);
    }
}
