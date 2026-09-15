package com.example.craftkitchen.listener;

import com.example.craftkitchen.CraftKitchen;
import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.cooking.CookingResult;
import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.RecipeDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 方塊互動 → 烹飪步驟推進。
 *
 * <p>為避免干擾原版方塊行為（工作台/釀造台會開介面），烹飪一律以「潛行右鍵」觸發。
 * 步驟 → 方塊對應：
 * <ul>
 *   <li>cut（切）→ 木製壓力板 / 石刃機</li>
 *   <li>marinate（醃）→ 釀造台</li>
 *   <li>cook（煮）→ 營火 / 靈魂營火</li>
 *   <li>season（調味）→ 工作台</li>
 * </ul>
 * 實際推進哪道食譜由 {@link CookingService#resolveStep} 決定（支援多食譜）。
 */
public class BlockInteractListener implements Listener {
    private final CraftKitchen plugin;
    private final CookingService cookingService;

    public BlockInteractListener(CraftKitchen plugin, CookingService cookingService) {
        this.plugin = plugin;
        this.cookingService = cookingService;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }

        String stepId = stepForBlock(event.getClickedBlock().getType());
        if (stepId == null) {
            return;
        }

        List<String> available = Arrays.stream(player.getInventory().getContents())
            .filter(item -> item != null && item.getType() != Material.AIR)
            .map(item -> item.getType().name())
            .toList();

        CookingService.StepResolution resolution =
            cookingService.resolveStep(player.getUniqueId(), stepId, available);
        if (resolution.recipeId() == null) {
            player.sendMessage("此處沒有可進行的食譜（步驟不對或缺少食材）。");
            return;
        }
        event.setCancelled(true);
        handleResult(player, resolution.recipeId(), resolution.result());
    }

    private String stepForBlock(Material type) {
        if (type == Material.OAK_PRESSURE_PLATE || type == Material.STONECUTTER) {
            return "cut";
        }
        if (type == Material.BREWING_STAND) {
            return "marinate";
        }
        if (type == Material.CAMPFIRE || type == Material.SOUL_CAMPFIRE) {
            return "cook";
        }
        if (type == Material.CRAFTING_TABLE) {
            return "season";
        }
        return null;
    }

    private void handleResult(Player player, String recipeId, CookingResult result) {
        if (result == CookingResult.COMPLETED) {
            consumeIngredients(player, recipeId);
            ItemStack reward = plugin.getItemProvider().createItem(recipeId, 1);
            CookingQuality quality = cookingService.getLastQuality(player.getUniqueId());
            var meta = reward.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(
                    QualityKeys.qualityKey(plugin),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    quality.name()
                );
                reward.setItemMeta(meta);
            }
            player.getInventory().addItem(reward);
            player.sendMessage("料理完成：" + recipeName(recipeId) + "，品質：" + quality.name());

            checkHiddenRecipe(player, recipeId);

            int xp = quality == CookingQuality.PERFECT ? 30 : 20;
            var levelUps = plugin.getLevelManager().addExperience(player.getUniqueId(), xp);
            for (int level : levelUps) {
                player.sendMessage("升級！料理等級達到 " + level
                    + "（" + plugin.getLevelManager().getTitle(player.getUniqueId()) + "）");
            }
        } else if (result == CookingResult.IN_PROGRESS) {
            var done = cookingService.getTracker().getCompletedSteps(player.getUniqueId(), recipeId);
            player.sendMessage(recipeName(recipeId) + " 進度：已完成 "
                + (done.isEmpty() ? "無" : String.join(", ", done)));
        } else if (result == CookingResult.NO_APPLICABLE_RECIPE) {
            player.sendMessage("此處沒有可進行的食譜。");
        }
    }

    private void consumeIngredients(Player player, String recipeId) {
        var recipe = cookingService.getRecipeManager().getRecipe(recipeId);
        if (recipe == null) {
            return;
        }
        for (String ingredient : recipe.getIngredients()) {
            Material material = Material.matchMaterial(ingredient);
            if (material != null) {
                player.getInventory().removeItem(new ItemStack(material, 1));
            }
        }
    }

    private void checkHiddenRecipe(Player player, String baseRecipeId) {
        var seasoningManager = plugin.getSeasoningManager();
        List<String> held = Arrays.stream(player.getInventory().getContents())
            .filter(item -> item != null && item.getType() != Material.AIR)
            .map(item -> item.getType().name())
            .toList();

        List<String> applied = new ArrayList<>();
        for (var entry : List.of("chili", "honey")) {
            var seasoning = seasoningManager.getSeasoning(entry);
            if (seasoning.isPresent() && held.contains(seasoning.get().getMaterial())) {
                applied.add(entry);
            }
        }

        seasoningManager.findHiddenRecipe(baseRecipeId, applied).ifPresent(hiddenId -> {
            ItemStack bonus = plugin.getItemProvider().createItem(hiddenId, 1);
            player.getInventory().addItem(bonus);
            player.sendMessage("發現隱藏食譜！獲得：" + recipeName(hiddenId));
        });
    }

    private String recipeName(String recipeId) {
        RecipeDefinition recipe = cookingService.getRecipeManager().getRecipe(recipeId);
        return recipe != null ? recipe.getName() : recipeId;
    }
}
