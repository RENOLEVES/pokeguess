package com.example.pokeguess.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.default-model:gemma2:9b}")
    private String defaultModel;

    @Value("${ollama.enabled:true}")
    private boolean ollamaEnabled;

    private final RestTemplate restTemplate;

    public OllamaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // check if ollama is available (railway deployment)
    private boolean isOllamaAvailable() {
        if (!ollamaEnabled) {
            return false;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");
            headers.set("User-Agent", "SpringBoot-Pokemon-Game");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(
                    ollamaUrl + "/api/tags",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return true;
        } catch (Exception e) {
            System.err.println("Ollama not available: " + e.getMessage());
            return false;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaGenerateRequest {
        private String model;
        private String prompt;
        private Boolean stream = false;
        private Map<String, Object> options = new HashMap<>();

        public OllamaGenerateRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
        }
    }

    @Data
    public static class OllamaGenerateResponse {
        private String model;

        @JsonProperty("created_at")
        private String createdAt;

        private String response;
        private Boolean done;

        @JsonProperty("context")
        private int[] context;

        @JsonProperty("total_duration")
        private Long totalDuration;

        @JsonProperty("load_duration")
        private Long loadDuration;

        @JsonProperty("prompt_eval_count")
        private Integer promptEvalCount;

        @JsonProperty("prompt_eval_duration")
        private Long promptEvalDuration;

        @JsonProperty("eval_count")
        private Integer evalCount;

        @JsonProperty("eval_duration")
        private Long evalDuration;
    }

    public String generate(String prompt) {
        return generate(prompt, defaultModel);
    }

    public String generate(String prompt, String modelName) {
        return generate(prompt, modelName, 0.3, 0.9, 1000);
    }

    public String generate(String prompt, String modelName,
                           double temperature, double topP, int maxTokens) {

        if (!isOllamaAvailable()) {
            System.out.println("Ollama service is not available.");
            return "AI service temporarily unavailable.";
        }

        OllamaGenerateRequest request = new OllamaGenerateRequest();
        request.setModel(modelName);
        request.setPrompt(prompt);
        request.setStream(false);

        Map<String, Object> options = new HashMap<>();
        options.put("temperature", temperature);
        options.put("top_p", topP);
        options.put("num_predict", maxTokens);
        options.put("repeat_penalty", 1.1);
        request.setOptions(options);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ngrok-skip-browser-warning", "true");
        headers.set("User-Agent", "SpringBoot-Pokemon-Game");

        HttpEntity<OllamaGenerateRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<OllamaGenerateResponse> response = restTemplate.postForEntity(
                    ollamaUrl + "/api/generate",
                    entity,
                    OllamaGenerateResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK &&
                    response.getBody() != null &&
                    response.getBody().getResponse() != null) {
                return response.getBody().getResponse().trim();
            }

        } catch (RestClientException e) {
            System.err.println("Ollama service unavailable: " + e.getMessage());
            return "AI service temporarily unavailable. Please try the basic hints!";
        }

        return "AI service temporarily unavailable.";
    }

    public String listModels() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("ngrok-skip-browser-warning", "true");
            headers.set("User-Agent", "SpringBoot-Pokemon-Game");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    ollamaUrl + "/api/tags",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "Failed to fetch model list: " + e.getMessage();
        }
    }

    public boolean modelExists(String modelName) {
        try {
            String models = listModels();
            return models.contains("\"name\":\"" + modelName + "\"");
        } catch (Exception e) {
            return false;
        }
    }

    public String pullModel(String modelName) {
        Map<String, String> request = Map.of("name", modelName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("ngrok-skip-browser-warning", "true");
        headers.set("User-Agent", "SpringBoot-Pokemon-Game");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    ollamaUrl + "/api/pull",
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "Failed to load model: " + e.getMessage();
        }
    }
}