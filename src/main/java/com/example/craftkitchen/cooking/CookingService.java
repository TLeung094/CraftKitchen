package com.example.craftkitchen.cooking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CookingService {
    private final RecipeManager recipeManager;
    private final CookingTracker tracker;
    private final Map<UUID, CookingQuality> lastQuality = new HashMap<>();
    private long sessionTimeoutMillis;
    private long perfectWindowMillis;

    public CookingService(RecipeManager recipeManager, CookingTracker tracker) {
        this.recipeManager = recipeManager;
        this.tracker = tracker;
    }

    public void setSessionTimeoutMillis(long sessionTimeoutMillis) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
    }

    public void setPerfectWindowMillis(long perfectWindowMillis) {
        this.perfectWindowMillis = perfectWindowMillis;
    }

    public CookingQuality getLastQuality(UUID playerId) {
        return lastQuality.getOrDefault(playerId, CookingQuality.NONE);
    }

    public CookingResult handleStep(String recipeId, String stepId) {
        RecipeDefinition recipe = recipeManager.getRecipe(recipeId);
        if (recipe == null) {
            return CookingResult.UNKNOWN_RECIPE;
        }
        if (!recipe.getSteps().contains(stepId)) {
            return CookingResult.INVALID_STEP;
        }

        tracker.start(recipeId);
        tracker.completeStep(recipeId, stepId);

        if (tracker.isCompleted(recipeId, recipe)) {
            tracker.reset(recipeId);
            return CookingResult.COMPLETED;
        }
        return CookingResult.IN_PROGRESS;
    }

    public CookingResult handleStep(UUID playerId, String recipeId, String stepId) {
        RecipeDefinition recipe = recipeManager.getRecipe(recipeId);
        if (recipe == null) {
            return CookingResult.UNKNOWN_RECIPE;
        }
        if (!recipe.getSteps().contains(stepId)) {
            return CookingResult.INVALID_STEP;
        }

        if (sessionTimeoutMillis > 0 && tracker.isSessionExpired(playerId, sessionTimeoutMillis)) {
            tracker.resetAll(playerId);
        }

        tracker.start(playerId, recipeId);
        tracker.completeStep(playerId, recipeId, stepId);

        if (tracker.isCompleted(playerId, recipeId, recipe)) {
            CookingQuality quality = evaluateQuality(playerId);
            lastQuality.put(playerId, quality);
            tracker.resetAll(playerId);
            return CookingResult.COMPLETED;
        }
        return CookingResult.IN_PROGRESS;
    }

    private CookingQuality evaluateQuality(UUID playerId) {
        if (perfectWindowMillis > 0 && tracker.getSessionElapsed(playerId) <= perfectWindowMillis) {
            return CookingQuality.PERFECT;
        }
        return CookingQuality.NORMAL;
    }

    public List<String> getMissingIngredients(String recipeId, List<String> availableMaterials) {
        RecipeDefinition recipe = recipeManager.getRecipe(recipeId);
        if (recipe == null) {
            return List.of();
        }

        return recipe.getIngredients().stream()
            .filter(ingredient -> !availableMaterials.contains(ingredient))
            .toList();
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public CookingTracker getTracker() {
        return tracker;
    }
}
