package com.example.craftkitchen;

import com.example.craftkitchen.social.SharedMealManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedMealTest {

    @Test
    void shouldDetectPlayerWithinRadius() {
        SharedMealManager manager = new SharedMealManager(5.0, 1.5);

        assertTrue(manager.isWithinRadius(0, 0, 0, 3, 0, 4));
        assertTrue(manager.isWithinRadius(0, 0, 0, 5, 0, 0));
        assertFalse(manager.isWithinRadius(0, 0, 0, 5.1, 0, 0));
        assertFalse(manager.isWithinRadius(0, 0, 0, 10, 10, 10));
    }

    @Test
    void shouldScaleDurationWhenSharing() {
        SharedMealManager manager = new SharedMealManager(5.0, 1.5);

        assertEquals(90, manager.scaleDuration(60, true));
        assertEquals(60, manager.scaleDuration(60, false));
    }

    @Test
    void shouldRoundScaledSharedDuration() {
        SharedMealManager manager = new SharedMealManager(5.0, 1.5);

        assertEquals(50, manager.scaleDuration(33, true));
    }
}
