package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingResult;
import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.CookingTracker;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.item.ItemMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResolveStepTest {
    private CookingService serviceWithDefaults() {
        return new CookingService(
            RecipeManager.fromConfig(ItemMode.VANILLA, new YamlConfiguration()),
            new CookingTracker()
        );
    }

    @Test
    void startsSmokedSteakWhenIngredientsPresentAndCutTriggered() {
        var service = serviceWithDefaults();
        UUID p = UUID.randomUUID();
        var res = service.resolveStep(p, "cut", List.of("BEEF", "PEPPER"));
        assertEquals("smoked_steak", res.recipeId());
        assertEquals(CookingResult.IN_PROGRESS, res.result());
    }

    @Test
    void completesFullSmokedSteakChain() {
        var service = serviceWithDefaults();
        UUID p = UUID.randomUUID();
        service.resolveStep(p, "cut", List.of("BEEF", "PEPPER"));
        service.resolveStep(p, "marinate", List.of("BEEF", "PEPPER"));
        var res = service.resolveStep(p, "cook", List.of("BEEF", "PEPPER"));
        assertEquals("smoked_steak", res.recipeId());
        assertEquals(CookingResult.COMPLETED, res.result());
    }

    @Test
    void noApplicableRecipeWhenIngredientsMissing() {
        var service = serviceWithDefaults();
        UUID p = UUID.randomUUID();
        var res = service.resolveStep(p, "cut", List.of());
        assertNull(res.recipeId());
        assertEquals(CookingResult.NO_APPLICABLE_RECIPE, res.result());
    }

    @Test
    void startsHerbSteakForDifferentIngredients() {
        var service = serviceWithDefaults();
        UUID p = UUID.randomUUID();
        var res = service.resolveStep(p, "cut", List.of("BEEF", "SWEET_BERRIES"));
        assertEquals("herb_steak", res.recipeId());
    }

    @Test
    void wrongStepForActiveRecipeYieldsNoApplicable() {
        var service = serviceWithDefaults();
        UUID p = UUID.randomUUID();
        service.resolveStep(p, "cut", List.of("BEEF", "PEPPER"));
        // smoked_steak 下一步是 marinate，對 cook 方塊應無可推進食譜
        var res = service.resolveStep(p, "cook", List.of("BEEF", "PEPPER"));
        assertNull(res.recipeId());
        assertEquals(CookingResult.NO_APPLICABLE_RECIPE, res.result());
    }

    @Test
    void nextStepReflectsProgress() {
        var service = serviceWithDefaults();
        UUID p = UUID.randomUUID();
        assertEquals("cut", service.nextStep(p, "smoked_steak"));
        service.resolveStep(p, "cut", List.of("BEEF", "PEPPER"));
        assertEquals("marinate", service.nextStep(p, "smoked_steak"));
    }
}
