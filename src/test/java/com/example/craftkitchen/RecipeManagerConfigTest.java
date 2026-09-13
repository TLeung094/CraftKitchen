package com.example.craftkitchen;

import com.example.craftkitchen.cooking.RecipeDefinition;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.item.ItemMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecipeManagerConfigTest {

    @Test
    void shouldLoadRecipesFromYamlConfiguration() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("recipes.smoked_steak.name", "煙燻牛排");
        config.set("recipes.smoked_steak.steps", List.of("cut", "marinate", "cook"));
        config.set("recipes.smoked_steak.ingredients", List.of("BEEF", "PEPPER"));
        config.set("recipes.herb_steak.name", "香草牛排");
        config.set("recipes.herb_steak.steps", List.of("cut", "cook", "season"));
        config.set("recipes.herb_steak.ingredients", List.of("BEEF", "SWEET_BERRIES"));

        RecipeManager manager = RecipeManager.fromConfig(ItemMode.VANILLA, config);

        RecipeDefinition smoked = manager.getRecipe("smoked_steak");
        assertNotNull(smoked);
        assertEquals("煙燻牛排", smoked.getName());
        assertEquals(List.of("cut", "marinate", "cook"), smoked.getSteps());
        assertEquals(List.of("BEEF", "PEPPER"), smoked.getIngredients());
        assertEquals(2, manager.getRecipes().size());
    }

    @Test
    void shouldFallBackToDefaultRecipesWhenConfigSectionMissing() {
        RecipeManager manager = RecipeManager.fromConfig(ItemMode.VANILLA, new YamlConfiguration());

        assertNotNull(manager.getRecipe("smoked_steak"));
        assertNotNull(manager.getRecipe("herb_steak"));
    }

    @Test
    void shouldIgnoreIncompleteRecipeEntries() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("recipes.broken_recipe.steps", List.of("cut"));

        RecipeManager manager = RecipeManager.fromConfig(ItemMode.VANILLA, config);

        assertNull(manager.getRecipe("broken_recipe"));
    }
}
