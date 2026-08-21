package com.kamsan.authorizationserver.controller;

import com.kamsan.authorizationserver.model.User;
import com.kamsan.authorizationserver.security.authentication.mfa.MfaAuthentication;
import com.kamsan.authorizationserver.service.UserService;
import com.kamsan.authorizationserver.sharedkernel.exception.ApiException;
import jakarta.servlet.RequestDispatcher;
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
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

import static com.kamsan.authorizationserver.utils.RequestUtils.getMessage;
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
        model.addAttribute("email", getAuthenticatedUserEmail(securityContext.getAuthentication()));
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

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        var errorException = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (errorException instanceof ApiException || errorException instanceof BadCredentialsException) {
            request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, errorException);
            return "login";
        }
        model.addAttribute("message", getMessage(request));
        return "error";
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

    private String getAuthenticatedUserEmail(@Nullable Authentication authentication) {
        return ((User) authentication.getPrincipal()).getEmail();
    }
}
