package com.example.pokeguess.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PokemonGuessDTO {
    private Integer id;
    private String imageUrl;
}