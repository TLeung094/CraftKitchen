package com.example.craftkitchen.command;

import com.example.craftkitchen.CraftKitchen;
import com.example.craftkitchen.cooking.RecipeDefinition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class KitchenCommand implements CommandExecutor, TabCompleter {
    private static final List<String> SUB_COMMANDS = List.of("recipe", "level", "mode", "reload", "give");

    private final CraftKitchen plugin;

    public KitchenCommand(CraftKitchen plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("CraftKitchen 指令：/kitchen <recipe|level|mode|reload|give>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "recipe" -> handleRecipe(sender);
            case "level" -> handleLevel(sender);
            case "mode" -> sender.sendMessage("目前物品模式：" + plugin.getItemMode().name());
            case "reload" -> handleReload(sender);
            case "give" -> handleGive(sender, args);
            default -> sender.sendMessage("未知子指令：" + args[0]);
        }
        return true;
    }

    private void handleRecipe(CommandSender sender) {
        if (!sender.hasPermission("craftkitchen.use")) {
            sender.sendMessage("你沒有權限使用此指令。");
            return;
        }
        sender.sendMessage("=== 可用食譜 ===");
        for (RecipeDefinition recipe : plugin.getCookingService().getRecipeManager().getRecipes()) {
            sender.sendMessage("- " + recipe.getName()
                + "（步驟：" + String.join(" → ", recipe.getSteps())
                + "，食材：" + String.join(", ", recipe.getIngredients()) + "）");
        }
    }

    private void handleLevel(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("此指令僅限玩家使用。");
            return;
        }
        if (!sender.hasPermission("craftkitchen.use")) {
            sender.sendMessage("你沒有權限使用此指令。");
            return;
        }
        var levelManager = plugin.getLevelManager();
        sender.sendMessage("料理等級：" + levelManager.getLevel(player.getUniqueId())
            + "（" + levelManager.getTitle(player.getUniqueId()) + "）"
            + "，經驗：" + levelManager.getExperience(player.getUniqueId()));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("craftkitchen.admin")) {
            sender.sendMessage("你沒有權限使用此指令。");
            return;
        }
        plugin.reloadPluginConfig();
        sender.sendMessage("CraftKitchen 配置已重新載入。");
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("此指令僅限玩家使用。");
            return;
        }
        if (!sender.hasPermission("craftkitchen.admin")) {
            sender.sendMessage("你沒有權限使用此指令。");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("用法：/kitchen give <food>");
            return;
        }
        ItemStack item = plugin.getItemProvider().createItem(args[1], 1);
        player.getInventory().addItem(item);
        sender.sendMessage("已給予：" + args[1]);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return SUB_COMMANDS.stream().filter(sub -> sub.startsWith(prefix)).toList();
        }
        return new ArrayList<>();
    }
}
