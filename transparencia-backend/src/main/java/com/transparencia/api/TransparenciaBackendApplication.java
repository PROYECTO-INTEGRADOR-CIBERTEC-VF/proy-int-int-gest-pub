package com.transparencia.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TransparenciaBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransparenciaBackendApplication.class, args);
    }

}
