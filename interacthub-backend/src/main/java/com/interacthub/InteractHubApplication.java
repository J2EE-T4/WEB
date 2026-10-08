package com.interacthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InteractHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(InteractHubApplication.class, args);
    }
}
