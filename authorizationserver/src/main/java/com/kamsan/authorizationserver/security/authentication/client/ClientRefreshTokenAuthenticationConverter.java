package com.kamsan.authorizationserver.security.authentication.client;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientRefreshTokenAuthenticationConverter implements AuthenticationConverter {
    @Override
    public @Nullable Authentication convert(HttpServletRequest request) {
        var grantType = request.getParameter(OAuth2ParameterNames.GRANT_TYPE);
        if (!AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(grantType)) {
            return null;
        } else {
            var clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
            if (!StringUtils.hasText(clientId)) {
                return null;
            }

            return new ClientRefreshTokenAuthentication(clientId);
        }
    }
}
