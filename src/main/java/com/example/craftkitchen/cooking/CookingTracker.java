package com.example.craftkitchen.cooking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

public class CookingTracker {
    private final Map<String, Set<String>> completedSteps = new HashMap<>();
    private final Map<UUID, Map<String, Set<String>>> playerSteps = new HashMap<>();
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final Map<UUID, Long> sessionStart = new HashMap<>();
    private final LongSupplier clock;

    public CookingTracker() {
        this(System::currentTimeMillis);
    }

    public CookingTracker(LongSupplier clock) {
        this.clock = clock;
    }

    public void start(String recipeId) {
        completedSteps.putIfAbsent(recipeId, new HashSet<>());
    }

    public void completeStep(String recipeId, String stepId) {
        completedSteps.computeIfAbsent(recipeId, key -> new HashSet<>()).add(stepId);
    }

    public List<String> getCompletedSteps(String recipeId) {
        return new ArrayList<>(completedSteps.getOrDefault(recipeId, new HashSet<>()));
    }

    public boolean isCompleted(String recipeId) {
        return completedSteps.containsKey(recipeId) && !completedSteps.get(recipeId).isEmpty();
    }

    public boolean isCompleted(String recipeId, RecipeDefinition recipe) {
        if (recipe == null) {
            return false;
        }

        Set<String> done = completedSteps.getOrDefault(recipeId, new HashSet<>());
        return recipe.getSteps().stream().allMatch(done::contains);
    }

    public void reset(String recipeId) {
        completedSteps.remove(recipeId);
    }

    public void start(UUID playerId, String recipeId) {
        playerSteps.computeIfAbsent(playerId, key -> new HashMap<>()).putIfAbsent(recipeId, new HashSet<>());
        sessionStart.putIfAbsent(playerId, clock.getAsLong());
        lastActivity.put(playerId, clock.getAsLong());
    }

    public void completeStep(UUID playerId, String recipeId, String stepId) {
        playerSteps.computeIfAbsent(playerId, key -> new HashMap<>())
            .computeIfAbsent(recipeId, key -> new HashSet<>())
            .add(stepId);
        lastActivity.put(playerId, clock.getAsLong());
    }

    public boolean isCompleted(UUID playerId, String recipeId, RecipeDefinition recipe) {
        if (recipe == null) {
            return false;
        }

        Set<String> done = playerSteps.getOrDefault(playerId, Map.of())
            .getOrDefault(recipeId, Set.of());
        return recipe.getSteps().stream().allMatch(done::contains);
    }

    public void reset(UUID playerId, String recipeId) {
        Map<String, Set<String>> recipes = playerSteps.get(playerId);
        if (recipes != null) {
            recipes.remove(recipeId);
        }
    }

    public void resetAll(UUID playerId) {
        playerSteps.remove(playerId);
        sessionStart.remove(playerId);
    }

    public long getSessionElapsed(UUID playerId) {
        Long start = sessionStart.get(playerId);
        return start == null ? 0L : clock.getAsLong() - start;
    }

    public boolean isSessionExpired(UUID playerId, long timeoutMillis) {
        Long last = lastActivity.get(playerId);
        return last != null && clock.getAsLong() - last > timeoutMillis;
    }
}
