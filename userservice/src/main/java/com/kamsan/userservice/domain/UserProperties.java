package com.kamsan.userservice.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user")
public record UserProperties(String imagesFolder) {

}
