package com.example.pokeguess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // 简单配置，适合大多数情况
        RestTemplate restTemplate = new RestTemplate();

        // 设置连接超时（可选）
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());

        return restTemplate;
    }

}