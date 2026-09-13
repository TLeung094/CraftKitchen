package com.example.craftkitchen;

import com.example.craftkitchen.food.FoodEffect;
import com.example.craftkitchen.seasoning.SeasoningManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeasoningManagerTest {

    @Test
    void shouldLoadSeasoningsFromYaml() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("seasonings.chili.name", "辣椒");
        config.set("seasonings.chili.material", "RED_DYE");
        config.set("seasonings.chili.effects", List.of(
            java.util.Map.of("type", "SPEED", "duration", 30, "amplifier", 0)
        ));

        SeasoningManager manager = SeasoningManager.fromConfig(config);

        assertTrue(manager.getSeasoning("chili").isPresent());
        assertEquals("辣椒", manager.getSeasoning("chili").orElseThrow().getName());
        assertEquals("RED_DYE", manager.getSeasoning("chili").orElseThrow().getMaterial());
        assertEquals(1, manager.getSeasoning("chili").orElseThrow().getEffects().size());
    }

    @Test
    void shouldCombineSeasoningEffects() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("seasonings.chili.name", "辣椒");
        config.set("seasonings.chili.material", "RED_DYE");
        config.set("seasonings.chili.effects", List.of(
            java.util.Map.of("type", "SPEED", "duration", 30, "amplifier", 0)
        ));
        config.set("seasonings.honey.name", "蜂蜜");
        config.set("seasonings.honey.material", "HONEY_BOTTLE");
        config.set("seasonings.honey.effects", List.of(
            java.util.Map.of("type", "REGENERATION", "duration", 5, "amplifier", 0)
        ));

        SeasoningManager manager = SeasoningManager.fromConfig(config);
        List<FoodEffect> bonus = manager.getBonusEffects(List.of("chili", "honey"));

        assertEquals(2, bonus.size());
        assertEquals("SPEED", bonus.get(0).getType());
        assertEquals("REGENERATION", bonus.get(1).getType());
    }

    @Test
    void shouldIgnoreUnknownSeasoningIds() {
        YamlConfiguration config = new YamlConfiguration();
        SeasoningManager manager = SeasoningManager.fromConfig(config);

        assertTrue(manager.getBonusEffects(List.of("not_real")).isEmpty());
    }

    @Test
    void shouldDetectHiddenRecipeFromSeasoningCombination() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("hidden-recipes.honey_spicy_steak.name", "蜜汁辣牛排");
        config.set("hidden-recipes.honey_spicy_steak.base", "smoked_steak");
        config.set("hidden-recipes.honey_spicy_steak.seasonings", List.of("chili", "honey"));

        SeasoningManager manager = SeasoningManager.fromConfig(config);

        assertEquals("honey_spicy_steak",
            manager.findHiddenRecipe("smoked_steak", List.of("chili", "honey")).orElseThrow());
        assertTrue(manager.findHiddenRecipe("smoked_steak", List.of("chili")).isEmpty());
        assertTrue(manager.findHiddenRecipe("herb_steak", List.of("chili", "honey")).isEmpty());
    }
}
