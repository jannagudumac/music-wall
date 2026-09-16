package com.musicwall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Starts Spring Boot, which finds the controllers, services, repositories and configuration.
@SpringBootApplication
public class MusicWallApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicWallApplication.class, args);
    }
}
