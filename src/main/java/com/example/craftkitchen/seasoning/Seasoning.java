package com.example.craftkitchen.seasoning;

import com.example.craftkitchen.food.FoodEffect;

import java.util.ArrayList;
import java.util.List;

public class Seasoning {
    private final String id;
    private final String name;
    private final String material;
    private final List<FoodEffect> effects;

    public Seasoning(String id, String name, String material, List<FoodEffect> effects) {
        this.id = id;
        this.name = name;
        this.material = material;
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

    public List<FoodEffect> getEffects() {
        return effects;
    }
}
