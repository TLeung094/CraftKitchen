package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingResult;
import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.CookingTracker;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.item.ItemMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookingServiceTest {

    @Test
    void shouldReturnCompletedOnlyAfterAllStepsAndResetProgress() {
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker());

        assertEquals(CookingResult.IN_PROGRESS, service.handleStep("smoked_steak", "cut"));
        assertEquals(CookingResult.IN_PROGRESS, service.handleStep("smoked_steak", "marinate"));
        assertEquals(CookingResult.COMPLETED, service.handleStep("smoked_steak", "cook"));

        assertTrue(service.getTracker().getCompletedSteps("smoked_steak").isEmpty());
    }

    @Test
    void shouldRejectUnknownRecipeAndInvalidStep() {
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker());

        assertEquals(CookingResult.UNKNOWN_RECIPE, service.handleStep("not_a_recipe", "cut"));
        assertEquals(CookingResult.INVALID_STEP, service.handleStep("smoked_steak", "fly"));
        assertTrue(service.getTracker().getCompletedSteps("smoked_steak").isEmpty());
    }
}
