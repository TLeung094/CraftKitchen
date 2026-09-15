package com.example.craftkitchen.item;

import com.example.craftkitchen.CraftKitchen;

public final class ItemProviderFactory {
    private ItemProviderFactory() {
    }

    public static ItemProvider create(ItemMode mode, CraftKitchen plugin) {
        return switch (mode) {
            case VANILLA -> new VanillaItemProvider(plugin);
            case CRAFTENGINE -> new CraftEngineItemProvider();
        };
    }
}
