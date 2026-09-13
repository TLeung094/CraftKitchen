package com.example.craftkitchen.listener;

import com.example.craftkitchen.CraftKitchen;
import com.example.craftkitchen.cooking.CookingQuality;
import com.example.craftkitchen.cooking.CookingResult;
import com.example.craftkitchen.cooking.CookingService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

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
        if (event.getClickedBlock() == null) {
            return;
        }

        Material blockType = event.getClickedBlock().getType();
        boolean isCuttingBoard = blockType == Material.OAK_PRESSURE_PLATE || blockType == Material.STONECUTTER;
        boolean isStove = blockType == Material.CAMPFIRE;
        if (!isCuttingBoard && !isStove) {
            return;
        }

        if (!hasIngredients(player, "smoked_steak")) {
            player.sendMessage("缺少食材，無法料理。");
            return;
        }

        if (isCuttingBoard) {
            CookingResult result = cookingService.handleStep(player.getUniqueId(), "smoked_steak", "cut");
            handleResult(player, result, "砧板互動已觸發");
        }

        if (isStove) {
            CookingResult result = cookingService.handleStep(player.getUniqueId(), "smoked_steak", "cook");
            handleResult(player, result, "爐灶互動已觸發");
        }

        if (cookingService.getRecipeManager().getRecipe("smoked_steak") != null) {
            player.sendMessage("當前食譜：" + cookingService.getRecipeManager().getRecipe("smoked_steak").getName());
        }
    }

    private boolean hasIngredients(Player player, String recipeId) {
        List<String> available = Arrays.stream(player.getInventory().getContents())
            .filter(item -> item != null && item.getType() != Material.AIR)
            .map(item -> item.getType().name())
            .toList();
        return cookingService.getMissingIngredients(recipeId, available).isEmpty();
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

    private void checkHiddenRecipe(Player player) {
        var seasoningManager = plugin.getSeasoningManager();
        List<String> held = Arrays.stream(player.getInventory().getContents())
            .filter(item -> item != null && item.getType() != Material.AIR)
            .map(item -> item.getType().name())
            .toList();

        List<String> applied = new java.util.ArrayList<>();
        for (var entry : List.of("chili", "honey")) {
            var seasoning = seasoningManager.getSeasoning(entry);
            if (seasoning.isPresent() && held.contains(seasoning.get().getMaterial())) {
                applied.add(entry);
            }
        }

        seasoningManager.findHiddenRecipe("smoked_steak", applied).ifPresent(hiddenId -> {
            ItemStack bonus = plugin.getItemProvider().createItem(hiddenId, 1);
            player.getInventory().addItem(bonus);
            player.sendMessage("發現隱藏食譜！獲得：" + hiddenId);
        });
    }

    private void handleResult(Player player, CookingResult result, String message) {
        player.sendMessage(message);
        if (result == CookingResult.COMPLETED) {
            consumeIngredients(player, "smoked_steak");
            ItemStack reward = plugin.getItemProvider().createItem("smoked_steak", 1);
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
            player.sendMessage("料理完成，品質：" + quality.name());

            checkHiddenRecipe(player);

            int xp = quality == CookingQuality.PERFECT ? 30 : 20;
            var levelUps = plugin.getLevelManager().addExperience(player.getUniqueId(), xp);
            for (int level : levelUps) {
                player.sendMessage("升級！料理等級達到 " + level + "（" + plugin.getLevelManager().getTitle(player.getUniqueId()) + "）");
            }
        }
    }
}
