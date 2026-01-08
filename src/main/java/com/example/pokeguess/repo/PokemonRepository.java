package com.example.pokeguess.repo;

import com.example.pokeguess.model.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {

    @Query(value = "SELECT * FROM pokemons ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Pokemon findRandomPokemon();
}