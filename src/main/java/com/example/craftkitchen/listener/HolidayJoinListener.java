package com.example.craftkitchen.listener;

import com.example.craftkitchen.CraftKitchen;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class HolidayJoinListener implements Listener {
    private static final Map<String, String> HOLIDAY_NAMES = Map.of(
        "mid_autumn", "中秋節",
        "christmas", "聖誕節",
        "new_year", "新年",
        "halloween", "萬聖節"
    );

    private final CraftKitchen plugin;

    public HolidayJoinListener(CraftKitchen plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getHolidayManager().getActiveHoliday().ifPresent(holidayId -> {
            String name = HOLIDAY_NAMES.getOrDefault(holidayId, holidayId);
            String food = plugin.getHolidayManager().getHolidayFood(holidayId).orElse(holidayId);
            event.getPlayer().sendMessage("今天是 " + name + "！節日限定料理「" + food + "」已開放製作。");
        });
    }
}
