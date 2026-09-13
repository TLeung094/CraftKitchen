package com.example.craftkitchen;

import com.example.craftkitchen.cooking.CookingResult;
import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.CookingTracker;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.item.ItemMode;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CookingTimeoutTest {

    @Test
    void shouldResetProgressWhenSessionExpires() {
        AtomicLong now = new AtomicLong(1_000L);
        CookingTracker tracker = new CookingTracker(now::get);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), tracker);
        service.setSessionTimeoutMillis(60_000L);
        UUID alice = UUID.randomUUID();

        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(alice, "smoked_steak", "cut"));
        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(alice, "smoked_steak", "marinate"));

        now.addAndGet(120_000L);

        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(alice, "smoked_steak", "cook"));
    }

    @Test
    void shouldCompleteNormallyWithinTimeout() {
        AtomicLong now = new AtomicLong(1_000L);
        CookingTracker tracker = new CookingTracker(now::get);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), tracker);
        service.setSessionTimeoutMillis(60_000L);
        UUID alice = UUID.randomUUID();

        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(alice, "smoked_steak", "cut"));
        now.addAndGet(30_000L);
        assertEquals(CookingResult.IN_PROGRESS, service.handleStep(alice, "smoked_steak", "marinate"));
        now.addAndGet(30_000L);
        assertEquals(CookingResult.COMPLETED, service.handleStep(alice, "smoked_steak", "cook"));
    }
}
