package com.example.craftkitchen.cooking;

import java.util.ArrayList;
import java.util.List;

public class RecipeDefinition {
    private final String id;
    private final String name;
    private final List<String> steps;
    private final List<String> ingredients;

    public RecipeDefinition(String id, String name, List<String> steps) {
        this(id, name, steps, List.of());
    }

    public RecipeDefinition(String id, String name, List<String> steps, List<String> ingredients) {
        this.id = id;
        this.name = name;
        this.steps = new ArrayList<>(steps);
        this.ingredients = new ArrayList<>(ingredients);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getSteps() {
        return steps;
    }

    public List<String> getIngredients() {
        return ingredients;
    }
}
