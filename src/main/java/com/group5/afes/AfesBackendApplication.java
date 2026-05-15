package com.group5.afes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AfesBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AfesBackendApplication.class, args);
    }

}
