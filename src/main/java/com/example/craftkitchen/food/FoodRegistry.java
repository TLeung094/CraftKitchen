package com.example.craftkitchen.food;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodRegistry {
    private final Map<String, FoodData> foods = new HashMap<>();

    public static FoodRegistry fromConfig(FileConfiguration config) {
        FoodRegistry registry = new FoodRegistry();
        ConfigurationSection foodsSection = config.getConfigurationSection("foods");
        if (foodsSection == null) {
            return registry;
        }

        for (String key : foodsSection.getKeys(false)) {
            ConfigurationSection section = foodsSection.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            String name = section.getString("name", key);
            String material = section.getString("material", "BREAD");
            int customModelData = section.getInt("custom-model-data", 0);

            List<FoodEffect> effects = new ArrayList<>();
            List<?> rawEffects = section.getList("effects", new ArrayList<>());
            if (rawEffects != null) {
                for (Object raw : rawEffects) {
                    if (raw instanceof Map<?, ?> map) {
                        Object typeValue = map.get("type");
                        String type = typeValue == null ? "NONE" : String.valueOf(typeValue);
                        int duration = getInt(map.get("duration"), 0);
                        int amplifier = getInt(map.get("amplifier"), 0);
                        effects.add(new FoodEffect(type, duration, amplifier));
                    }
                }
            }

            registry.foods.put(key, new FoodData(key, name, material, customModelData, effects));
        }

        return registry;
    }

    public FoodData get(String id) {
        return foods.get(id);
    }

    private static int getInt(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
