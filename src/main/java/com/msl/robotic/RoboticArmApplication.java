package com.msl.robotic;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.msl.*")
public class RoboticArmApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoboticArmApplication.class, args);
    }
}