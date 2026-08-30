package com.kamsan.authorizationserver.security.listener;

import com.kamsan.authorizationserver.model.User;
import com.kamsan.authorizationserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import static com.kamsan.authorizationserver.utils.UserAgentUtils.*;
import static com.kamsan.authorizationserver.utils.UserUtils.getUser;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiAuthenticationEventListener {
    private final UserService userService;
    private final HttpServletRequest request;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        log.info("Authentication success - {}", event);
        if (event.getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
            User user = getUser(event.getAuthentication());
            userService.setLastLogin(user.getUserId());
            userService.resetLoginAttempts(user.getUserPublicId());
            userService.addLoginDevice(user.getUserId(), getDevice(request), getClient(request), getIpAddress(request));
        }
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        log.info("Authentication failure - {}", event);
        log.info("Authentication exception class : {}", event.getException().getClass());
        if (event.getException() instanceof BadCredentialsException) {
            String userEmail = (String) event.getAuthentication().getPrincipal();
            userService.updateLoginAttempts(userEmail);
        }
    }
}
