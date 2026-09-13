package com.example.craftkitchen.cooking;

import com.example.craftkitchen.item.ItemMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {
    private final ItemMode mode;
    private final Map<String, RecipeDefinition> recipes = new HashMap<>();

    public RecipeManager(ItemMode mode) {
        this.mode = mode;
        registerDefaultRecipes();
    }

    public static RecipeManager fromConfig(ItemMode mode, FileConfiguration config) {
        RecipeManager manager = new RecipeManager(mode);
        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return manager;
        }

        manager.recipes.clear();
        for (String id : recipesSection.getKeys(false)) {
            ConfigurationSection section = recipesSection.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            String name = section.getString("name");
            List<String> steps = section.getStringList("steps");
            if (name == null || name.isBlank() || steps.isEmpty()) {
                continue;
            }
            List<String> ingredients = section.getStringList("ingredients");
            manager.recipes.put(id, new RecipeDefinition(id, name, steps, ingredients));
        }
        return manager;
    }

    private void registerDefaultRecipes() {
        recipes.put("smoked_steak", new RecipeDefinition(
            "smoked_steak",
            "煙燻牛排",
            List.of("cut", "marinate", "cook"),
            List.of("BEEF", "PEPPER")
        ));

        recipes.put("herb_steak", new RecipeDefinition(
            "herb_steak",
            "香草牛排",
            List.of("cut", "cook", "season"),
            List.of("BEEF", "SWEET_BERRIES")
        ));
    }

    public List<RecipeDefinition> getRecipes() {
        return new ArrayList<>(recipes.values());
    }

    public RecipeDefinition getRecipe(String id) {
        return recipes.get(id);
    }

    public ItemMode getMode() {
        return mode;
    }
}
