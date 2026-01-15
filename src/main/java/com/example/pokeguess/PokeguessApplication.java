package com.example.pokeguess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PokeguessApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokeguessApplication.class, args);
        System.out.println("=========================================");
        System.out.println("宝可梦游戏后端已启动!");
        System.out.println("Ollama URL: http://localhost:11435");
        System.out.println("Spring Boot URL: http://localhost:8888");
        System.out.println("测试端点: http://localhost:8888/api/test/health");
        System.out.println("=========================================");
    }
}
