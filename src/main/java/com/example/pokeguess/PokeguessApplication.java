package com.example.pokeguess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PokeguessApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokeguessApplication.class, args);
        System.out.println("=========================================");
        System.out.println("Backend launching!");
        System.out.println("Ollama URL: http://localhost:11435");
        System.out.println("Spring Boot URL: http://localhost:8888");
        System.out.println("=========================================");
    }
}
