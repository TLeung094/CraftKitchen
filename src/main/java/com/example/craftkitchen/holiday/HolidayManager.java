package com.example.craftkitchen.holiday;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class HolidayManager {
    private record HolidayWindow(MonthDay start, MonthDay end, String foodId) {
        boolean contains(LocalDate date) {
            MonthDay day = MonthDay.from(date);
            if (!start.isAfter(end)) {
                return !day.isBefore(start) && !day.isAfter(end);
            }
            return !day.isBefore(start) || !day.isAfter(end);
        }
    }

    private final Map<String, HolidayWindow> holidays = new LinkedHashMap<>();
    private final Supplier<LocalDate> dateSupplier;
    private boolean enabled = true;

    public HolidayManager() {
        this(LocalDate::now);
    }

    public HolidayManager(Supplier<LocalDate> dateSupplier) {
        this.dateSupplier = dateSupplier;

        // 中秋：以國曆 9/15–10/8 近似（實際為農曆八月十五）
        holidays.put("mid_autumn", new HolidayWindow(MonthDay.of(9, 15), MonthDay.of(10, 8), "mooncake"));
        // 聖誕：12/24–12/26
        holidays.put("christmas", new HolidayWindow(MonthDay.of(12, 24), MonthDay.of(12, 26), "christmas_pudding"));
        // 新年：12/31–1/2（跨年窗口）
        holidays.put("new_year", new HolidayWindow(MonthDay.of(12, 31), MonthDay.of(1, 2), "new_year_feast"));
        // 萬聖節：10/30–11/1
        holidays.put("halloween", new HolidayWindow(MonthDay.of(10, 30), MonthDay.of(11, 1), "pumpkin_soup"));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHolidayActive(String holidayId) {
        if (!enabled) {
            return false;
        }
        HolidayWindow window = holidays.get(holidayId);
        return window != null && window.contains(dateSupplier.get());
    }

    public Optional<String> getActiveHoliday() {
        if (!enabled) {
            return Optional.empty();
        }
        LocalDate today = dateSupplier.get();
        return holidays.entrySet().stream()
            .filter(entry -> entry.getValue().contains(today))
            .map(Map.Entry::getKey)
            .findFirst();
    }

    public Optional<String> getHolidayFood(String holidayId) {
        HolidayWindow window = holidays.get(holidayId);
        return window == null ? Optional.empty() : Optional.of(window.foodId());
    }
}
