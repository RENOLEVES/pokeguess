package com.example.pokeguess.util;

import com.example.pokeguess.model.Pokemon;
import com.example.pokeguess.repo.PokemonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PokemonRepository repository;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) return;

        RestTemplate restTemplate = new RestTemplate();
        for (int i = 1; i <= 151; i++) {

            // get basic information
            String url = "https://pokeapi.co/api/v2/pokemon/" + i;
            Map<String, Object> data = restTemplate.getForObject(url, Map.class);

            // get name
            String speciesUrl = "https://pokeapi.co/api/v2/pokemon-species/" + i;
            Map<String, Object> speciesData = restTemplate.getForObject(speciesUrl, Map.class);
            List<Map<String, Object>> names = (List<Map<String, Object>>) speciesData.get("names");
            String nameZh = names.stream()
                    .filter(n -> ((Map)n.get("language")).get("name").equals("zh-Hans"))
                    .findFirst()
                    .map(n -> n.get("name").toString())
                    .orElse("undefined");

            // save to postgres
            Pokemon p = new Pokemon();
            p.setId(i);
            p.setNameEn(data.get("name").toString());
            p.setNameZh(nameZh);
            p.setImageUrl(String.format("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png", i));
            repository.save(p);

            System.out.println("sychronized: " + nameZh);
        }
    }
}