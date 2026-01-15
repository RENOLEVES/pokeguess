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

    private final RestTemplate restTemplate;

    public OllamaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // 请求实体
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaGenerateRequest {
        private String model;
        private String prompt;
        private Boolean stream = false;
        private Map<String, Object> options = new HashMap<>();

        // 简化构造函数
        public OllamaGenerateRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
        }
    }

    // 响应实体
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

    /**
     * 生成文本
     * @param prompt 提示词
     * @return 生成的文本
     */
    public String generate(String prompt) {
        return generate(prompt, defaultModel);
    }

    /**
     * 生成文本（指定模型）
     * @param prompt 提示词
     * @param modelName 模型名称
     * @return 生成的文本
     */
    public String generate(String prompt, String modelName) {
        return generate(prompt, modelName, 0.3, 0.9, 1000);
    }

    /**
     * 生成文本（完整参数）
     */
    public String generate(String prompt, String modelName,
                           double temperature, double topP, int maxTokens) {

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
            throw new RuntimeException("调用Ollama API失败: " + e.getMessage(), e);
        }

        return "抱歉，AI服务暂时不可用。";
    }

    /**
     * 列出所有可用模型
     */
    public String listModels() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    ollamaUrl + "/api/tags",
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "获取模型列表失败: " + e.getMessage();
        }
    }

    /**
     * 检查模型是否存在
     */
    public boolean modelExists(String modelName) {
        try {
            String models = listModels();
            return models.contains("\"name\":\"" + modelName + "\"");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 拉取模型
     */
    public String pullModel(String modelName) {
        Map<String, String> request = Map.of("name", modelName);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    ollamaUrl + "/api/pull",
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "拉取模型失败: " + e.getMessage();
        }
    }
}