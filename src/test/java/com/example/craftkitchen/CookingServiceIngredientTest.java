package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.CookingTracker;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.item.ItemMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookingServiceIngredientTest {

    @Test
    void shouldReportMissingIngredients() {
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker());

        List<String> missing = service.getMissingIngredients("smoked_steak", List.of("BEEF"));

        assertEquals(List.of("PEPPER"), missing);
    }

    @Test
    void shouldReturnEmptyWhenAllIngredientsPresent() {
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker());

        List<String> missing = service.getMissingIngredients("smoked_steak", List.of("BEEF", "PEPPER"));

        assertTrue(missing.isEmpty());
    }

    @Test
    void shouldReturnEmptyForUnknownRecipe() {
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker());

        assertTrue(service.getMissingIngredients("not_a_recipe", List.of()).isEmpty());
    }
}
