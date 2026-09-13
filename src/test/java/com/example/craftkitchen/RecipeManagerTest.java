package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingTracker;
import com.example.craftkitchen.cooking.RecipeDefinition;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.item.ItemMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeManagerTest {

    @Test
    void shouldOnlyConsiderRecipeCompleteAfterAllRequiredSteps() {
        RecipeManager manager = new RecipeManager(ItemMode.VANILLA);
        RecipeDefinition recipe = manager.getRecipe("smoked_steak");
        CookingTracker tracker = new CookingTracker();

        tracker.start("smoked_steak");
        tracker.completeStep("smoked_steak", "cut");
        assertFalse(tracker.isCompleted("smoked_steak", recipe));

        tracker.completeStep("smoked_steak", "marinate");
        tracker.completeStep("smoked_steak", "cook");
        assertTrue(tracker.isCompleted("smoked_steak", recipe));
    }
}
