package com.example.pokeguess.service;

import com.example.pokeguess.dto.PokemonGuessDTO;
import com.example.pokeguess.model.Pokemon;
import com.example.pokeguess.repo.PokemonRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PokemonGameService {

    private final PokemonRepository pokemonRepository;
    private final OllamaService ollamaService;
    private final RestTemplate restTemplate;

    private final Map<Integer, Map<String, String>> speciesCache = new HashMap<>();

    public PokemonGameService(PokemonRepository pokemonRepository,
                              OllamaService ollamaService,
                              RestTemplate restTemplate) {
        this.pokemonRepository = pokemonRepository;
        this.ollamaService = ollamaService;
        this.restTemplate = restTemplate;
    }

    /**
     * Get a random Pokemon quiz question
     */
    public PokemonGuessDTO getRandomQuiz() {
        Pokemon pokemon = pokemonRepository.findRandomPokemon();
        return new PokemonGuessDTO(pokemon.getId(), pokemon.getImageUrl());
    }

    /**
     * Check if the user's answer is correct
     */
    public boolean checkAnswer(Integer id, String userAnswer) {
        return pokemonRepository.findById(id)
                .map(p -> p.getNameZh().equalsIgnoreCase(userAnswer.trim()) ||
                        p.getNameEn().equalsIgnoreCase(userAnswer.trim()))
                .orElse(false);
    }

    /**
     * Get the correct name for a Pokemon
     */
    public String getCorrectName(Integer id) {
        return pokemonRepository.findById(id)
                .map(p -> p.getNameEn() + " (" + p.getNameZh() + ")")
                .orElse("Unknown");
    }

    /**
     * Get hints for a Pokemon (type, height, weight, color)
     */
    public Map<String, String> getHints(Integer id) {
        Pokemon pokemon = pokemonRepository.findById(id).orElse(null);
        if (pokemon == null) {
            return null;
        }

        Map<String, String> hints = new HashMap<>();

        // Type hint
        String type = pokemon.getType1();
        if (pokemon.getType2() != null && !pokemon.getType2().isEmpty()) {
            type += "/" + pokemon.getType2();
        }
        hints.put("type1", type);

        // Get additional data from PokeAPI if not cached
        Map<String, String> speciesData = getSpeciesData(id);
        hints.put("height", speciesData.getOrDefault("height", "Unknown"));
        hints.put("weight", speciesData.getOrDefault("weight", "Unknown"));
        hints.put("color", speciesData.getOrDefault("color", "Unknown"));

        return hints;
    }

    /**
     * Get species data from PokeAPI (with caching)
     */
    private Map<String, String> getSpeciesData(Integer id) {
        // Check cache first
        if (speciesCache.containsKey(id)) {
            return speciesCache.get(id);
        }

        Map<String, String> data = new HashMap<>();

        try {
            // Get Pokemon data for height/weight
            String pokemonUrl = "https://pokeapi.co/api/v2/pokemon/" + id;
            Map<String, Object> pokemonData = restTemplate.getForObject(pokemonUrl, Map.class);

            if (pokemonData != null) {
                Integer height = (Integer) pokemonData.get("height");
                Integer weight = (Integer) pokemonData.get("weight");

                // Convert to readable format (height in decimeters, weight in hectograms)
                data.put("height", String.format("%.1f m", height / 10.0));
                data.put("weight", String.format("%.1f kg", weight / 10.0));
            }

            // Get species data for color
            String speciesUrl = "https://pokeapi.co/api/v2/pokemon-species/" + id;
            Map<String, Object> speciesData = restTemplate.getForObject(speciesUrl, Map.class);

            if (speciesData != null) {
                Map<String, String> colorData = (Map<String, String>) speciesData.get("color");
                if (colorData != null) {
                    data.put("color", capitalize(colorData.get("name")));
                }
            }

            // Cache the result
            speciesCache.put(id, data);

        } catch (Exception e) {
            System.err.println("Error fetching species data for Pokemon " + id + ": " + e.getMessage());
            data.put("height", "Unknown");
            data.put("weight", "Unknown");
            data.put("color", "Unknown");
        }

        return data;
    }

    /**
     * Get AI-generated hint using Ollama
     */
    public String getAiHint(Integer id, String language) {
        Pokemon pokemon = pokemonRepository.findById(id).orElse(null);
        if (pokemon == null) {
            return null;
        }

        String prompt;
        String fallbackHint;

        if ("zh".equals(language)) {
            // Chinese prompt
            prompt = String.format("""
                你正在为一个宝可梦猜谜游戏提供提示。这个宝可梦是 %s（%s）。
                
                请给出一个创意有趣的提示，要求：
                1. 不要直接提到宝可梦的名字
                2. 描述它的外观、行为或栖息地
                3. 可以提到它的知名技能或特点
                4. 1-2句话
                5. 有帮助但不要太明显
                
                提示示例：
                - "这只电气鼠以其电击能力和红色脸颊而闻名。"
                - "一只背着壳到处走的水龟，会从嘴里喷水。"
                - "这只喷火蜥蜴尾巴上的火焰代表着它的生命力。"
                
                请用中文给出关于 %s 的提示（但不要说出名字）：
                """, pokemon.getNameEn(), pokemon.getNameZh(), pokemon.getNameZh());
            fallbackHint = "这只宝可梦有着独特的特征，让它与众不同！";
        } else {
            // English prompt
            prompt = String.format("""
                You are giving a hint for a Pokemon guessing game. The Pokemon is %s.
                
                Give a creative, interesting hint that:
                1. Does NOT mention the Pokemon's name directly
                2. References its appearance, behavior, or habitat
                3. Might mention what it's known for or famous moves
                4. Is 1-2 sentences long
                5. Is helpful but not too obvious
                
                Example hints:
                - "This electric mouse is known for its shocking personality and red cheeks."
                - "A water turtle that carries its shell wherever it goes and shoots water from its mouth."
                - "This fire-breathing lizard has a flame on its tail that shows its life force."
                
                Give a hint for %s (but don't say the name):
                """, pokemon.getNameEn(), pokemon.getNameEn());
            fallbackHint = "This Pokemon has unique characteristics that make it special!";
        }

        try {
            return ollamaService.generate(prompt, "gemma2:9b", 0.7, 0.9, 150);
        } catch (Exception e) {
            System.err.println("Error generating AI hint: " + e.getMessage());
            return fallbackHint;
        }
    }

    /**
     * Capitalize first letter
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}