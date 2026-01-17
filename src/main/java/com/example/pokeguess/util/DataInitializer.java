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
        if (repository.count() > 0) {
            System.out.println("Database already initialized with " + repository.count() + " Pokemon.");
            return;
        }

        System.out.println("Initializing Pokemon database...");
        RestTemplate restTemplate = new RestTemplate();

        for (int i = 1; i <= 151; i++) {
            try {
                // Get basic Pokemon information
                String url = "https://pokeapi.co/api/v2/pokemon/" + i;
                Map<String, Object> data = restTemplate.getForObject(url, Map.class);

                if (data == null) continue;

                // Get Chinese name from species endpoint
                String speciesUrl = "https://pokeapi.co/api/v2/pokemon-species/" + i;
                Map<String, Object> speciesData = restTemplate.getForObject(speciesUrl, Map.class);

                String nameZh = "未知";
                if (speciesData != null) {
                    List<Map<String, Object>> names = (List<Map<String, Object>>) speciesData.get("names");
                    nameZh = names.stream()
                            .filter(n -> {
                                Map<String, String> lang = (Map<String, String>) n.get("language");
                                return "zh-Hans".equals(lang.get("name"));
                            })
                            .findFirst()
                            .map(n -> n.get("name").toString())
                            .orElse("未知");
                }

                // Extract types
                List<Map<String, Object>> types = (List<Map<String, Object>>) data.get("types");
                String type1 = null;
                String type2 = null;

                for (Map<String, Object> typeEntry : types) {
                    Map<String, String> type = (Map<String, String>) typeEntry.get("type");
                    int slot = (Integer) typeEntry.get("slot");

                    if (slot == 1) {
                        type1 = capitalize(type.get("name"));
                    } else if (slot == 2) {
                        type2 = capitalize(type.get("name"));
                    }
                }

                // Create and save Pokemon
                Pokemon p = new Pokemon();
                p.setId(i);
                p.setNameEn(capitalize(data.get("name").toString()));
                p.setNameZh(nameZh);
                p.setImageUrl(String.format(
                        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/%d.png",
                        i
                ));
                p.setType1(type1);
                p.setType2(type2);

                repository.save(p);

                System.out.println(String.format("Synchronized #%d: %s (%s) - Type: %s%s",
                        i, p.getNameEn(), nameZh, type1,
                        type2 != null ? "/" + type2 : ""));

                // Small delay to avoid overwhelming the API
                Thread.sleep(100);

            } catch (Exception e) {
                System.err.println("Error loading Pokemon #" + i + ": " + e.getMessage());
            }
        }

        System.out.println("Database initialization complete! Total Pokemon: " + repository.count());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}