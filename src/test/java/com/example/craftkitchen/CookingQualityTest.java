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

class CookingQualityTest {

    @Test
    void shouldGrantPerfectQualityWhenCompletedWithinLimit() {
        AtomicLong now = new AtomicLong(0L);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker(now::get));
        service.setPerfectWindowMillis(30_000L);
        UUID alice = UUID.randomUUID();

        service.handleStep(alice, "smoked_steak", "cut");
        now.addAndGet(10_000L);
        service.handleStep(alice, "smoked_steak", "marinate");
        now.addAndGet(10_000L);
        service.handleStep(alice, "smoked_steak", "cook");

        assertEquals(CookingQuality.PERFECT, service.getLastQuality(alice));
    }

    @Test
    void shouldGrantNormalQualityWhenCompletedSlowly() {
        AtomicLong now = new AtomicLong(0L);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker(now::get));
        service.setPerfectWindowMillis(30_000L);
        service.setSessionTimeoutMillis(600_000L);
        UUID alice = UUID.randomUUID();

        service.handleStep(alice, "smoked_steak", "cut");
        now.addAndGet(60_000L);
        service.handleStep(alice, "smoked_steak", "marinate");
        now.addAndGet(60_000L);
        service.handleStep(alice, "smoked_steak", "cook");

        assertEquals(CookingQuality.NORMAL, service.getLastQuality(alice));
    }

    @Test
    void shouldClearQualityOnNewSession() {
        AtomicLong now = new AtomicLong(0L);
        CookingService service = new CookingService(new RecipeManager(ItemMode.VANILLA), new CookingTracker(now::get));
        UUID alice = UUID.randomUUID();

        assertEquals(CookingQuality.NONE, service.getLastQuality(alice));
    }
}
