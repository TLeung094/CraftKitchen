package com.example.craftkitchen.food;

import java.util.ArrayList;
import java.util.List;

public class FoodData {
    private final String id;
    private final String name;
    private final String material;
    private final int customModelData;
    private final List<FoodEffect> effects;

    public FoodData(String id, String name, String material, int customModelData, List<FoodEffect> effects) {
        this.id = id;
        this.name = name;
        this.material = material;
        this.customModelData = customModelData;
        this.effects = effects == null ? new ArrayList<>() : new ArrayList<>(effects);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public List<FoodEffect> getEffects() {
        return effects;
    }
}
