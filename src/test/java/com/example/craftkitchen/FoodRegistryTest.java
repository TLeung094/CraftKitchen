package com.example.craftkitchen;

import com.example.craftkitchen.food.FoodData;
import com.example.craftkitchen.food.FoodRegistry;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FoodRegistryTest {

    @Test
    void shouldLoadFoodFromYamlConfiguration() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("foods.smoked_steak.name", "煙燻牛排");
        config.set("foods.smoked_steak.material", "COOKED_BEEF");
        config.set("foods.smoked_steak.custom-model-data", 10201);
        config.set("foods.smoked_steak.effects", List.of(
            Map.of("type", "SPEED", "duration", 30, "amplifier", 0)
        ));

        FoodRegistry registry = FoodRegistry.fromConfig(config);
        FoodData food = registry.get("smoked_steak");

        assertNotNull(food);
        assertEquals("煙燻牛排", food.getName());
        assertEquals("COOKED_BEEF", food.getMaterial());
        assertEquals(10201, food.getCustomModelData());
        assertEquals(1, food.getEffects().size());
        assertEquals("SPEED", food.getEffects().getFirst().getType());
    }
}
