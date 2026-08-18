package com.kamsan.authorizationserver.controller;

import com.kamsan.authorizationserver.model.User;
import com.kamsan.authorizationserver.security.MfaAuthentication;
import com.kamsan.authorizationserver.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import static com.kamsan.authorizationserver.utils.UserUtils.getUser;

@Controller
@AllArgsConstructor
public class LoginController {
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final AuthenticationFailureHandler authenticationFailureHandler = new SimpleUrlAuthenticationFailureHandler("/mfa?error");
    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/mfa")
    public String mfa(Model model, @CurrentSecurityContext SecurityContext securityContext) {
        model.addAttribute("email", getAuthenticatedUser(securityContext.getAuthentication()));
        return "mfa";
    }

    @PostMapping("/mfa")
    public void validateCode(@RequestParam("code") String code, HttpServletRequest request, HttpServletResponse response, @CurrentSecurityContext SecurityContext securityContext) throws ServletException, IOException {
        var user = getUser(securityContext.getAuthentication());
        if (userService.isValidQRCode(user.getPublicId(), code)) {
            this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, getSavedAuthentication(request, response));
            return;
        } else {
            this.authenticationFailureHandler.onAuthenticationFailure(request, response, new BadCredentialsException("Invalid QR code. Please try again"));
        }
    }

    /**
     * On récupère l'authentification sauvegardée par le loginSuccesHandler. L'objet authentification est le même
     * car il provient de la même request, donc le même thread.
     *
     * @param request
     * @param response
     * @return
     */
    private Authentication getSavedAuthentication(HttpServletRequest request, HttpServletResponse response) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        MfaAuthentication mfaAuthentication = (MfaAuthentication) securityContext.getAuthentication();
        securityContext.setAuthentication(mfaAuthentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
        return mfaAuthentication.getPrimaryAuthentication();
    }

    private @Nullable Object getAuthenticatedUser(@Nullable Authentication authentication) {
        return ((User) authentication.getPrincipal()).getEmail();
    }
}
