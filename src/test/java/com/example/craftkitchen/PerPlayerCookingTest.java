package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingResult;
import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.CookingTracker;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.item.ItemMode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerPlayerCookingTest {

    @Test
    void shouldTrackProgressIndependentlyPerPlayer() {
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker());
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();

        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(alice, "smoked_steak", "cut"));
        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(alice, "smoked_steak", "marinate"));
        assertEquals(CookingResult.COMPLETED, service.handleStep(alice, "smoked_steak", "cook"));

        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(bob, "smoked_steak", "cut"));
        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(bob, "smoked_steak", "marinate"));
        assertEquals(CookingResult.COMPLETED, service.handleStep(bob, "smoked_steak", "cook"));
    }

    @Test
    void shouldNotLetOnePlayersStepsCompleteAnotherPlayersRecipe() {
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker());
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();

        service.handleStep(alice, "smoked_steak", "cut");
        service.handleStep(alice, "smoked_steak", "marinate");

        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(bob, "smoked_steak", "cook"));
    }
}
