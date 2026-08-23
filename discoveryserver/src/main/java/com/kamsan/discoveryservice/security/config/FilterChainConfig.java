package com.kamsan.discoveryservice.security.config;

import com.kamsan.discoveryservice.security.handler.DiscoveryAccessDeniedHandler;
import com.kamsan.discoveryservice.security.handler.DiscoveryAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static com.kamsan.discoveryservice.constants.Roles.APP_READ;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FilterChainConfig {
    private final DiscoveryUserDetailsService discoveryUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http.csrf(csrf -> csrf.ignoringRequestMatchers("/eureka/**"))
            .userDetailsService(discoveryUserDetailsService)
            .exceptionHandling(exception -> exception.accessDeniedHandler(new DiscoveryAccessDeniedHandler()))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/eureka/fonts/**", "/eureka/css/**", "/eureka/js/**", "/eureka/images/**", "/icon/**")
                    .permitAll()
                    .requestMatchers("/eureka/**")
                    .hasAnyAuthority(APP_READ)
                    .requestMatchers("/**")
                    .hasAnyAuthority(APP_READ)
                    .anyRequest()
                    .authenticated())
            .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(new DiscoveryAuthenticationEntryPoint()));

        return http.build();
    }
}
