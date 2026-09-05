package com.kamsan.userservice.resource;

import com.kamsan.userservice.domain.ApiResponse;
import com.kamsan.userservice.dto.CreateUserDTO;
import com.kamsan.userservice.dto.ReadUserDTO;
import com.kamsan.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.kamsan.userservice.utils.RequestUtils.getResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserResource {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid CreateUserDTO createUserDTO) {
        this.userService.createUser(createUserDTO);
        return ResponseEntity.created(getUri()).body(getResponse(
                Collections.emptyMap(),
                "Account created. Check your email to enable your account.",
                HttpStatus.CREATED));
    }

    private URI getUri() {
        return URI.create("/profile/<userId>");
    }

    @GetMapping("/verify/account")
    public ResponseEntity<ApiResponse> verifyAccount(@RequestParam("token") String token) {
        userService.verifyAccount(token);
        return ResponseEntity.ok().body(getResponse(
                Collections.emptyMap(),
                "Account verified. You may login now.",
                HttpStatus.OK));
    }

    @GetMapping("/mfa/enable")
    public ResponseEntity<ApiResponse> enableMfa(Authentication authentication) {
        ReadUserDTO userDTO = userService.enableMfa(UUID.fromString(authentication.getName()));
        return ResponseEntity.ok().body(getResponse(
                Map.of("user", userDTO),
                "MFA is now successfully enabled",
                HttpStatus.OK));
    }
}
