package com.example.pokeguess.controller;

import com.example.pokeguess.dto.PokemonGuessDTO;
import com.example.pokeguess.service.PokemonGameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pokemon")
@CrossOrigin(origins = "*")
public class PokemonGameController {

    private final PokemonGameService pokemonGameService;

    public PokemonGameController(PokemonGameService pokemonGameService) {
        this.pokemonGameService = pokemonGameService;
    }

    /**
     * Get a random Pokemon quiz question (ID and image only)
     */
    @GetMapping("/quiz")
    public ResponseEntity<PokemonGuessDTO> getQuiz() {
        PokemonGuessDTO quiz = pokemonGameService.getRandomQuiz();
        return ResponseEntity.ok(quiz);
    }

    /**
     * Check if the user's answer is correct
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAnswer(@RequestBody Map<String, Object> request) {
        Integer id = (Integer) request.get("id");
        String userAnswer = (String) request.get("userAnswer");

        if (id == null || userAnswer == null || userAnswer.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ID and answer are required"));
        }

        boolean isCorrect = pokemonGameService.checkAnswer(id, userAnswer);
        String correctName = pokemonGameService.getCorrectName(id);

        return ResponseEntity.ok(Map.of(
                "correct", isCorrect,
                "correctName", correctName
        ));
    }

    /**
     * Get hints for a specific Pokemon (type, height, weight, color)
     */
    @GetMapping("/hints/{id}")
    public ResponseEntity<Map<String, String>> getHints(@PathVariable Integer id) {
        Map<String, String> hints = pokemonGameService.getHints(id);

        if (hints == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(hints);
    }

    /**
     * Get AI-generated hint for a Pokemon
     */
    @GetMapping("/ai-hint/{id}")
    public ResponseEntity<Map<String, String>> getAiHint(@PathVariable Integer id) {
        String hint = pokemonGameService.getAiHint(id);

        if (hint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("hint", hint));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "Pokemon Guess Game"
        ));
    }
}