package com.example.craftkitchen.seasoning;

import com.example.craftkitchen.food.FoodEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SeasoningManager {
    private final Map<String, Seasoning> seasonings = new LinkedHashMap<>();
    private final Map<String, HiddenRecipe> hiddenRecipes = new LinkedHashMap<>();

    public static SeasoningManager fromConfig(FileConfiguration config) {
        SeasoningManager manager = new SeasoningManager();

        ConfigurationSection seasoningsSection = config.getConfigurationSection("seasonings");
        if (seasoningsSection != null) {
            for (String id : seasoningsSection.getKeys(false)) {
                ConfigurationSection section = seasoningsSection.getConfigurationSection(id);
                if (section == null) {
                    continue;
                }
                String name = section.getString("name");
                String material = section.getString("material");
                if (name == null || material == null) {
                    continue;
                }
                manager.seasonings.put(id, new Seasoning(id, name, material, parseEffects(section)));
            }
        }

        ConfigurationSection hiddenSection = config.getConfigurationSection("hidden-recipes");
        if (hiddenSection != null) {
            for (String id : hiddenSection.getKeys(false)) {
                ConfigurationSection section = hiddenSection.getConfigurationSection(id);
                if (section == null) {
                    continue;
                }
                String name = section.getString("name");
                String base = section.getString("base");
                List<String> required = section.getStringList("seasonings");
                if (name == null || base == null || required.isEmpty()) {
                    continue;
                }
                manager.hiddenRecipes.put(id, new HiddenRecipe(id, name, base, required));
            }
        }
        return manager;
    }

    private static List<FoodEffect> parseEffects(ConfigurationSection section) {
        List<FoodEffect> effects = new ArrayList<>();
        List<?> raw = section.getList("effects");
        if (raw == null) {
            return effects;
        }
        for (Object entry : raw) {
            if (!(entry instanceof Map<?, ?> map)) {
                continue;
            }
            Object type = map.get("type");
            if (type == null) {
                continue;
            }
            int duration = map.get("duration") instanceof Number n ? n.intValue() : 30;
            int amplifier = map.get("amplifier") instanceof Number n ? n.intValue() : 0;
            effects.add(new FoodEffect(type.toString(), duration, amplifier));
        }
        return effects;
    }

    public Optional<Seasoning> getSeasoning(String id) {
        return Optional.ofNullable(seasonings.get(id));
    }

    public List<FoodEffect> getBonusEffects(List<String> seasoningIds) {
        List<FoodEffect> bonus = new ArrayList<>();
        for (String id : seasoningIds) {
            Seasoning seasoning = seasonings.get(id);
            if (seasoning != null) {
                bonus.addAll(seasoning.getEffects());
            }
        }
        return bonus;
    }

    public Optional<String> findHiddenRecipe(String baseRecipeId, List<String> appliedSeasonings) {
        Set<String> applied = new HashSet<>(appliedSeasonings);
        return hiddenRecipes.values().stream()
            .filter(recipe -> recipe.getBaseRecipeId().equals(baseRecipeId))
            .filter(recipe -> applied.containsAll(recipe.getSeasonings()))
            .map(HiddenRecipe::getId)
            .findFirst();
    }
}
