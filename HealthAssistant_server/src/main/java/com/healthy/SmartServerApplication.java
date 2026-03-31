package com.healthy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SmartServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartServerApplication.class, args);
    }

}
