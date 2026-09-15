package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.CookingTracker;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.item.ItemMode;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CookingQualityTest {

    @Test
    void shouldRollFailedForLowestRoll() {
        AtomicLong now = new AtomicLong(0L);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker(now::get));
        service.setPerfectWindowMillis(30_000L);
        service.setSessionTimeoutMillis(300_000L);
        service.setRandomSource(() -> 0.0);
        UUID alice = UUID.randomUUID();

        service.handleStep(alice, "smoked_steak", "cut");
        now.addAndGet(5_000L);
        service.handleStep(alice, "smoked_steak", "marinate");
        now.addAndGet(5_000L);
        service.handleStep(alice, "smoked_steak", "cook");

        assertEquals(CookingQuality.FAILED, service.getLastQuality(alice));
    }

    @Test
    void shouldRollLegendaryForHighestRollWhenFast() {
        AtomicLong now = new AtomicLong(0L);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker(now::get));
        service.setPerfectWindowMillis(30_000L);
        service.setSessionTimeoutMillis(300_000L);
        service.setRandomSource(() -> 0.999999);
        UUID alice = UUID.randomUUID();

        service.handleStep(alice, "smoked_steak", "cut");
        service.handleStep(alice, "smoked_steak", "marinate");
        service.handleStep(alice, "smoked_steak", "cook");

        assertEquals(CookingQuality.LEGENDARY, service.getLastQuality(alice));
    }

    @Test
    void shouldClearQualityOnNewSession() {
        AtomicLong now = new AtomicLong(0L);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker(now::get));
        UUID alice = UUID.randomUUID();

        assertEquals(CookingQuality.NONE, service.getLastQuality(alice));
    }

    @Test
    void shouldNeverRollNoneOnCompletion() {
        AtomicLong now = new AtomicLong(0L);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker(now::get));
        service.setPerfectWindowMillis(30_000L);
        service.setSessionTimeoutMillis(300_000L);
        UUID alice = UUID.randomUUID();

        for (double roll = 0.0; roll < 1.0; roll += 0.05) {
            final double r = roll;
            service.setRandomSource(() -> r);
            service.handleStep(alice, "smoked_steak", "cut");
            now.addAndGet(1_000L);
            service.handleStep(alice, "smoked_steak", "marinate");
            now.addAndGet(1_000L);
            service.handleStep(alice, "smoked_steak", "cook");
            assertNotEquals(CookingQuality.NONE, service.getLastQuality(alice));
        }
    }
}
