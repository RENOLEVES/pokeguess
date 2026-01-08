package com.example.pokeguess.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pokemons")
@Data
public class Pokemon {
    @Id
    private Integer id;

    @Column(nullable = false)
    private String nameEn;

    @Column(nullable = false)
    private String nameZh;

    @Column(nullable = false)
    private String imageUrl;

    private String type1;
    private String type2;
}