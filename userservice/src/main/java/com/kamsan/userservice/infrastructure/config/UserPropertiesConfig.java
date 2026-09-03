package com.kamsan.userservice.infrastructure.config;

import com.kamsan.userservice.domain.UserProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UserProperties.class)
public class UserPropertiesConfig {
}
