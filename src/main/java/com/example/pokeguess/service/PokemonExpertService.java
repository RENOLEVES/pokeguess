package com.example.pokeguess.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PokemonExpertService {

    private final OllamaService ollamaService;

    // Cache for common questions
    private final Map<String, String> responseCache = new HashMap<>();

    public PokemonExpertService(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    /**
     * Pokemon Q&A
     */
    public String askAboutPokemon(String question) {
        // Check cache
        String cacheKey = question.trim().toLowerCase();
        if (responseCache.containsKey(cacheKey)) {
            return responseCache.get(cacheKey);
        }

        String systemPrompt = """
            You are a Pokemon encyclopedia expert. Answer in English.
            
            Requirements:
            1. Provide accurate, detailed official information
            2. Include interesting background stories and ecological habits
            3. If there are related legends or fun facts, provide them
            4. Answers should be vivid and interesting, suitable for game players
            5. Give complete answers, don't cut off mid-sentence
            
            Question: %s
            
            Please provide your answer:
            """.formatted(question);

        String response = ollamaService.generate(systemPrompt, "gemma2:9b", 0.7, 0.9, 2000);

        // Cache the result (simple cache, consider Redis for production)
        if (responseCache.size() > 1000) {
            responseCache.clear();
        }
        responseCache.put(cacheKey, response);

        return response;
    }

    /**
     * Get detailed Pokemon information
     */
    public String getPokemonDetail(String pokemonName) {
        String prompt = String.format("""
            Please provide a detailed introduction to the Pokemon: %s
            
            Include the following information:
            1. Basic attributes: Type, height, weight, abilities
            2. Evolution chain: Complete evolution path and evolution conditions
            3. Signature moves: 3-5 commonly used moves and their effects
            4. Battle role: Role in battles and common tactics
            5. Background story: Pokedex description and ecological habits
            6. Fun facts: Interesting trivia or little-known facts
            
            Please present in clear English, organized by sections with headers.
            Make it easy for new players to understand.
            """, pokemonName);

        return askAboutPokemon(prompt);
    }

    /**
     * Get battle advice
     */
    public String getBattleAdvice(String attacker, String defender) {
        String prompt = String.format("""
            Please analyze a Pokemon battle: %s VS %s
            
            Include:
            1. Type matchup analysis - Who has the advantage?
            2. Recommended move sets - What moves should %s use?
            3. Tactical advice - Specific battle strategies
            4. Important notes - What should you watch out for from the opponent?
            
            Please explain clearly in English with bullet points.
            """, attacker, defender, attacker);

        return askAboutPokemon(prompt);
    }

    /**
     * Generate Pokemon story
     */
    public String generatePokemonStory(String pokemonName, String setting) {
        String prompt = String.format("""
            Please create an adventure story about %s.
            
            Story setting: %s
            
            Requirements:
            1. Include at least 3 other Pokemon characters
            2. Have a clear conflict and resolution
            3. Showcase %s's characteristics and abilities
            4. Story should be vivid and suitable for all ages
            5. Length around 500-800 words
            
            Please begin the story:
            """, pokemonName, setting, pokemonName);

        return askAboutPokemon(prompt);
    }

    /**
     * Clear cache (for use after model update)
     */
    public void clearCache() {
        responseCache.clear();
    }
}