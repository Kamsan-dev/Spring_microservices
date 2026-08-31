package com.kamsan.userservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
public class Application {

    @Value("${ui.app.url}")
    private String redirectUri;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
