package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingStep;
import com.example.craftkitchen.cooking.CookingTracker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookingTrackerTest {

    @Test
    void shouldTrackCookingProgress() {
        CookingTracker tracker = new CookingTracker();

        tracker.start("smoked_steak");
        tracker.completeStep("smoked_steak", "cut");
        tracker.completeStep("smoked_steak", "cook");

        assertEquals(2, tracker.getCompletedSteps("smoked_steak").size());
        assertTrue(tracker.isCompleted("smoked_steak"));
    }

    @Test
    void shouldInitializeDefaultRecipeSteps() {
        CookingTracker tracker = new CookingTracker();
        CookingStep step = new CookingStep("prep", "prepare ingredients");

        assertEquals("prep", step.getId());
        assertEquals("prepare ingredients", step.getDescription());
    }
}
