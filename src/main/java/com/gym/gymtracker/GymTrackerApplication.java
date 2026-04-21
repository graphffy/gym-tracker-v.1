package com.gym.gymtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class GymTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymTrackerApplication.class, args);
    }
}
