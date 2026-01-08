package com.example.pokeguess.service;

import com.example.pokeguess.dto.PokemonGuessDTO;
import com.example.pokeguess.model.Pokemon;
import com.example.pokeguess.repo.PokemonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PokemonService {

    @Autowired
    private PokemonRepository pokemonRepository;

    public PokemonGuessDTO getRandomQuiz() {
        Pokemon pokemon = pokemonRepository.findRandomPokemon();
        return new PokemonGuessDTO(pokemon.getId(), pokemon.getImageUrl());
    }

    public boolean checkAnswer(Integer id, String userAnswer) {
        return pokemonRepository.findById(id)
                .map(p -> p.getNameZh().equals(userAnswer) || p.getNameEn().equalsIgnoreCase(userAnswer))
                .orElse(false);
    }
}