package com.example.craftkitchen.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class LevelManager {
    private final Map<UUID, Integer> experience = new HashMap<>();
    private final TreeMap<Integer, String> titles = new TreeMap<>();
    private boolean enabled = true;

    public LevelManager() {
        titles.put(1, "見習廚師");
        titles.put(5, "家常廚師");
        titles.put(10, "宴會廚師");
        titles.put(20, "傳奇廚神");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setExperience(UUID playerId, int amount) {
        experience.put(playerId, amount);
    }

    public Map<UUID, Integer> snapshot() {
        return new HashMap<>(experience);
    }

    public int getExperience(UUID playerId) {
        return experience.getOrDefault(playerId, 0);
    }

    public int getLevel(UUID playerId) {
        return levelFor(getExperience(playerId));
    }

    public int levelFor(int xp) {
        if (xp < 0) {
            return 1;
        }
        return xp / 50 + 1;
    }

    public String getTitle(UUID playerId) {
        int level = getLevel(playerId);
        return titles.floorEntry(level).getValue();
    }

    public List<Integer> addExperience(UUID playerId, int amount) {
        if (!enabled || amount <= 0) {
            return List.of();
        }

        int before = getLevel(playerId);
        int total = getExperience(playerId) + amount;
        experience.put(playerId, total);
        int after = levelFor(total);

        List<Integer> levelUps = new ArrayList<>();
        for (int level = before + 1; level <= after; level++) {
            levelUps.add(level);
        }
        return levelUps;
    }
}
