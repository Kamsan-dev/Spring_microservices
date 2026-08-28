package com.kamsan.discoveryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @Bean
//    public CommandLineRunner startup(BCryptPasswordEncoder encoder) {
//        return args -> {
//            String password = encoder.encode("");
//            System.out.println(password);
//        };
//    }
}
