package com.example.ecommercial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
@SpringBootApplication
public class ECommercialApplication {
    public static void main(String[] args) {
        SpringApplication.run(ECommercialApplication.class, args);
    }
}
