package com.example.mentalwellnessbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MentalWellnessBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(MentalWellnessBotApplication.class, args);
    }
}
