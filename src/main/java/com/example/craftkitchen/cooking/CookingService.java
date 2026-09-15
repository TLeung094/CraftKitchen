package com.example.craftkitchen.cooking;

import com.example.craftkitchen.level.LevelManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class CookingService {
    private final RecipeManager recipeManager;
    private final CookingTracker tracker;
    private final Map<UUID, CookingQuality> lastQuality = new HashMap<>();
    private long sessionTimeoutMillis;
    private long perfectWindowMillis;
    private QualityCalculator qualityCalculator = new QualityCalculator();
    private LevelManager levelManager;
    private Supplier<Double> randomSource = Math::random;

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

    public void setQualityCalculator(QualityCalculator qualityCalculator) {
        this.qualityCalculator = qualityCalculator != null ? qualityCalculator : new QualityCalculator();
    }

    public void setLevelManager(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    public void setRandomSource(Supplier<Double> randomSource) {
        this.randomSource = randomSource != null ? randomSource : Math::random;
    }

    public QualityCalculator getQualityCalculator() {
        return qualityCalculator;
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
        long elapsed = tracker.getSessionElapsed(playerId);
        int level = levelManager == null ? 1 : levelManager.getLevel(playerId);
        return qualityCalculator.roll(elapsed, perfectWindowMillis, sessionTimeoutMillis, level, randomSource.get());
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

    /**
     * 回傳玩家在指定食譜中「下一個未完成步驟」，若全數完成或食譜不存在回 null。
     */
    public String nextStep(UUID playerId, String recipeId) {
        RecipeDefinition recipe = recipeManager.getRecipe(recipeId);
        if (recipe == null) {
            return null;
        }
        List<String> done = tracker.getCompletedSteps(playerId, recipeId);
        for (String step : recipe.getSteps()) {
            if (!done.contains(step)) {
                return step;
            }
        }
        return null;
    }

    /**
     * 決定玩家對「某步驟方塊」操作時應推進哪道食譜。
     *
     * <p>策略：優先推進玩家已進行中且下一步吻合的食譜；若無，則嘗試啟動「第一步吻合且食材齊全」的新食譜。
     * 此方法為純邏輯（不碰 Bukkit），可單元測試。
     */
    public StepResolution resolveStep(UUID playerId, String stepId, List<String> availableMaterials) {
        for (String recipeId : tracker.getActiveRecipes(playerId)) {
            String next = nextStep(playerId, recipeId);
            if (next != null && next.equals(stepId)) {
                return new StepResolution(recipeId, handleStep(playerId, recipeId, stepId));
            }
        }
        for (RecipeDefinition recipe : recipeManager.getRecipes()) {
            if (recipe.getSteps().isEmpty()) {
                continue;
            }
            if (!recipe.getSteps().get(0).equals(stepId)) {
                continue;
            }
            if (!getMissingIngredients(recipe.getId(), availableMaterials).isEmpty()) {
                continue;
            }
            return new StepResolution(recipe.getId(), handleStep(playerId, recipe.getId(), stepId));
        }
        return new StepResolution(null, CookingResult.NO_APPLICABLE_RECIPE);
    }

    /** resolveStep 的結果：recipeId 為 null 表示無可推進食譜。 */
    public record StepResolution(String recipeId, CookingResult result) {
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public CookingTracker getTracker() {
        return tracker;
    }
}
