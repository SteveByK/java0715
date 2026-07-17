package com.stevebyk.java0715;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
/**
 * Spring Boot entry point for the banking application.
 */
public class Java0715Application {

    public static void main(String[] args) {
        SpringApplication.run(Java0715Application.class, args);
    }
}
