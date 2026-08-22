package com.kamsan.discoveryservice.security.authentication.user;

import com.kamsan.discoveryservice.model.User;
import com.kamsan.discoveryservice.service.implementation.UserServiceImpl;
import com.kamsan.discoveryservice.sharedkernel.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated;
import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;

@RequiredArgsConstructor
@Component
public class UserAuthenticationProvider implements AuthenticationProvider {
    private final UserServiceImpl userService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            User user = userService.getUserByEmail((String) authentication.getPrincipal());
            validateUser.accept(user);
            if (passwordEncoder.matches((String) authentication.getCredentials(), user.getPassword())) {
                // 3ème parametre -> liste de GrantedAuthorities, exemple : role_admin,ticket:create,ticket:delete...
                // return new UsernamePasswordAuthenticationToken
                return authenticated(user, "[PROTECTED]", commaSeparatedStringToAuthorityList(user.getRole() + "," + user.getAuthorities()));
            } else {
                throw new BadCredentialsException("Incorrect email/password. Please try again.");
            }
        } catch (BadCredentialsException | ApiException | LockedException | DisabledException exception) {
            throw new ApiException(exception.getMessage());
        } catch (Exception exception) {
            throw new ApiException("Unable to authenticate. Please try again");
        }
    }

    /**
     * Informe l'authenticationManager que ce provider gére les UsernamePasswordAuthenticationToken
     *
     * @param authentication
     * @return
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private final Consumer<User> validateUser = user -> {
        if (user.isAccountLocked() || user.getLoginAttempts() >= 5) {
            throw new LockedException(String.format(user.getLoginAttempts() > 0
                    ? "Account currently locked after %s failed login attempts"
                    : "Account currently locked", user.getLoginAttempts()));
        }
        if (!user.isAccountEnabled()) {
            throw new DisabledException("Your account is currently disabled");
        }
        if (user.isAccountExpired()) {
            throw new DisabledException("Your account has expired. Please contact administration");
        }
    };
}
