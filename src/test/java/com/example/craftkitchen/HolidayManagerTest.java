package com.example.craftkitchen;

import com.example.craftkitchen.holiday.HolidayManager;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HolidayManagerTest {

    @Test
    void shouldDetectChristmasWindow() {
        HolidayManager manager = new HolidayManager(() -> LocalDate.of(2026, 12, 25));

        assertTrue(manager.isHolidayActive("christmas"));
        assertFalse(manager.isHolidayActive("halloween"));
        assertEquals("christmas", manager.getActiveHoliday().orElseThrow());
    }

    @Test
    void shouldDetectHalloweenAndNewYearWindows() {
        assertTrue(new HolidayManager(() -> LocalDate.of(2026, 10, 31)).isHolidayActive("halloween"));
        assertTrue(new HolidayManager(() -> LocalDate.of(2027, 1, 1)).isHolidayActive("new_year"));
        assertFalse(new HolidayManager(() -> LocalDate.of(2026, 9, 13)).getActiveHoliday().isPresent());
    }

    @Test
    void shouldDetectMidAutumnByLunarEquivalentApproximation() {
        HolidayManager manager = new HolidayManager(() -> LocalDate.of(2026, 9, 25));

        assertTrue(manager.isHolidayActive("mid_autumn"));
    }

    @Test
    void shouldProvideHolidayFoodId() {
        HolidayManager manager = new HolidayManager(() -> LocalDate.of(2026, 12, 25));

        assertEquals("christmas_pudding", manager.getHolidayFood("christmas").orElseThrow());
        assertTrue(manager.getHolidayFood("halloween").isPresent());
    }

    @Test
    void shouldDisableAllHolidaysWhenFlagOff() {
        HolidayManager manager = new HolidayManager(() -> LocalDate.of(2026, 12, 25));
        manager.setEnabled(false);

        assertFalse(manager.isHolidayActive("christmas"));
        assertTrue(manager.getActiveHoliday().isEmpty());
    }
}
