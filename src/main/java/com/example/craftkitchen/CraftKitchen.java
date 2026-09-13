package com.example.craftkitchen;

import com.example.craftkitchen.command.KitchenCommand;
import com.example.craftkitchen.config.ConfigManager;
import com.example.craftkitchen.config.ModeDetector;
import com.example.craftkitchen.cooking.CookingService;
import com.example.craftkitchen.cooking.CookingTracker;
import com.example.craftkitchen.cooking.RecipeManager;
import com.example.craftkitchen.database.PlayerDataStore;
import com.example.craftkitchen.food.FoodRegistry;
import com.example.craftkitchen.holiday.HolidayManager;
import com.example.craftkitchen.listener.BlockInteractListener;
import com.example.craftkitchen.listener.ConsumeListener;
import com.example.craftkitchen.listener.HolidayJoinListener;
import com.example.craftkitchen.level.LevelManager;
import com.example.craftkitchen.seasoning.SeasoningManager;
import com.example.craftkitchen.social.SharedMealManager;
import com.example.craftkitchen.item.ItemMode;
import com.example.craftkitchen.item.ItemProvider;
import com.example.craftkitchen.item.ItemProviderFactory;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftKitchen extends JavaPlugin {
    private static CraftKitchen instance;

    private ConfigManager configManager;
    private ItemMode itemMode;
    private ItemProvider itemProvider;
    private FoodRegistry foodRegistry;
    private CookingService cookingService;
    private SharedMealManager sharedMealManager;
    private LevelManager levelManager;
    private HolidayManager holidayManager;
    private PlayerDataStore playerDataStore;
    private SeasoningManager seasoningManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.itemMode = ModeDetector.detect(this);
        this.itemProvider = ItemProviderFactory.create(this.itemMode);
        this.foodRegistry = FoodRegistry.fromConfig(getConfig());
        this.cookingService = new CookingService(RecipeManager.fromConfig(this.itemMode, getConfig()), new CookingTracker());
        this.cookingService.setSessionTimeoutMillis(getConfig().getLong("settings.cooking-timeout-seconds", 300L) * 1000L);
        this.cookingService.setPerfectWindowMillis(getConfig().getLong("settings.perfect-window-seconds", 30L) * 1000L);
        this.sharedMealManager = new SharedMealManager(
            getConfig().getDouble("settings.shared-radius", 5.0),
            getConfig().getDouble("settings.shared-multiplier", 1.5)
        );
        this.levelManager = new LevelManager();
        this.levelManager.setEnabled(getConfig().getBoolean("settings.level-enabled", true));
        this.holidayManager = new HolidayManager();
        this.holidayManager.setEnabled(getConfig().getBoolean("settings.holiday-enabled", true));
        this.seasoningManager = SeasoningManager.fromConfig(getConfig());

        try {
            getDataFolder().mkdirs();
            this.playerDataStore = new PlayerDataStore(
                new java.io.File(getDataFolder(), getConfig().getString("database.file", "data.db"))
            );
            var loaded = this.playerDataStore.loadAll();
            for (var entry : loaded.entrySet()) {
                this.levelManager.setExperience(entry.getKey(), entry.getValue());
            }
            getLogger().info("已從資料庫載入 " + loaded.size() + " 筆玩家資料。");
        } catch (java.sql.SQLException e) {
            getLogger().severe("資料庫初始化失敗，等級資料將無法保存：" + e.getMessage());
        }

        getServer().getPluginManager().registerEvents(new ConsumeListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockInteractListener(this, this.cookingService), this);
        getServer().getPluginManager().registerEvents(new HolidayJoinListener(this), this);

        var kitchenCommand = new KitchenCommand(this);
        getCommand("kitchen").setExecutor(kitchenCommand);
        getCommand("kitchen").setTabCompleter(kitchenCommand);

        getLogger().info("CraftKitchen 已啟動，模式：" + itemMode.name());
    }

    @Override
    public void onDisable() {
        if (playerDataStore != null) {
            try {
                for (var entry : levelManager.snapshot().entrySet()) {
                    playerDataStore.saveExperience(entry.getKey(), entry.getValue());
                }
                playerDataStore.close();
            } catch (java.sql.SQLException e) {
                getLogger().severe("資料庫保存失敗：" + e.getMessage());
            }
        }
        getLogger().info("CraftKitchen 已停用");
    }

    public static CraftKitchen getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ItemMode getItemMode() {
        return itemMode;
    }

    public ItemProvider getItemProvider() {
        return itemProvider;
    }

    public FoodRegistry getFoodRegistry() {
        return foodRegistry;
    }

    public CookingService getCookingService() {
        return cookingService;
    }

    public SharedMealManager getSharedMealManager() {
        return sharedMealManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public HolidayManager getHolidayManager() {
        return holidayManager;
    }

    public SeasoningManager getSeasoningManager() {
        return seasoningManager;
    }

    public void reloadPluginConfig() {
        reloadConfig();
        this.foodRegistry = FoodRegistry.fromConfig(getConfig());
        this.cookingService = new CookingService(RecipeManager.fromConfig(this.itemMode, getConfig()), new CookingTracker());
        this.cookingService.setSessionTimeoutMillis(getConfig().getLong("settings.cooking-timeout-seconds", 300L) * 1000L);
        this.cookingService.setPerfectWindowMillis(getConfig().getLong("settings.perfect-window-seconds", 30L) * 1000L);
        this.levelManager.setEnabled(getConfig().getBoolean("settings.level-enabled", true));
        this.holidayManager.setEnabled(getConfig().getBoolean("settings.holiday-enabled", true));
    }
}
