package com.example.pokeguess.controller;

import com.example.pokeguess.dto.PokemonGuessDTO;
import com.example.pokeguess.service.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pokemon")
@CrossOrigin(origins = "http://localhost:3000")
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/random")
    public PokemonGuessDTO getRandom() {
        return pokemonService.getRandomQuiz();
    }

    @PostMapping("/check")
    public Map<String, Boolean> check(@RequestBody Map<String, String> payload) {
        Integer id = Integer.parseInt(payload.get("id"));
        String answer = payload.get("answer");
        boolean isCorrect = pokemonService.checkAnswer(id, answer);
        return Map.of("correct", isCorrect);
    }
}