package com.example.craftkitchen.item;

public final class ItemProviderFactory {
    private ItemProviderFactory() {
    }

    public static ItemProvider create(ItemMode mode) {
        return switch (mode) {
            case VANILLA -> new VanillaItemProvider();
            case CRAFTENGINE -> new CraftEngineItemProvider();
        };
    }
}
