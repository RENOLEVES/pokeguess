package com.example.pokeguess.controller;

import com.example.pokeguess.service.PokemonExpertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/pokemon")
@CrossOrigin(origins = "*") // 根据前端地址调整
public class PokemonController {

    private final PokemonExpertService pokemonExpertService;

    public PokemonController(PokemonExpertService pokemonExpertService) {
        this.pokemonExpertService = pokemonExpertService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "this question can't be empty"));
        }

        String answer = pokemonExpertService.askAboutPokemon(question);
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    @GetMapping("/detail/{name}")
    public ResponseEntity<Map<String, String>> getPokemonDetail(@PathVariable String name) {
        String detail = pokemonExpertService.getPokemonDetail(name);
        return ResponseEntity.ok(Map.of(
                "pokemon", name,
                "detail", detail
        ));
    }

    @GetMapping("/battle-advice")
    public ResponseEntity<Map<String, String>> getBattleAdvice(
            @RequestParam String attacker,
            @RequestParam String defender) {

        String advice = pokemonExpertService.getBattleAdvice(attacker, defender);
        return ResponseEntity.ok(Map.of(
                "attacker", attacker,
                "defender", defender,
                "advice", advice
        ));
    }

    @PostMapping("/generate-story")
    public ResponseEntity<Map<String, String>> generateStory(
            @RequestBody Map<String, String> request) {

        String pokemonName = request.get("pokemon");
        String setting = request.get("setting");

        if (pokemonName == null || setting == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Pokemon name needed."));
        }

        String story = pokemonExpertService.generatePokemonStory(pokemonName, setting);
        return ResponseEntity.ok(Map.of(
                "pokemon", pokemonName,
                "story", story
        ));
    }

    @GetMapping("/test")
    public String test() {
        return "Controller is working!";
    }
}