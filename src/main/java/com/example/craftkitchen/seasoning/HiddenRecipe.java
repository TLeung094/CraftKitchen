package com.example.craftkitchen.seasoning;

import java.util.ArrayList;
import java.util.List;

public class HiddenRecipe {
    private final String id;
    private final String name;
    private final String baseRecipeId;
    private final List<String> seasonings;

    public HiddenRecipe(String id, String name, String baseRecipeId, List<String> seasonings) {
        this.id = id;
        this.name = name;
        this.baseRecipeId = baseRecipeId;
        this.seasonings = new ArrayList<>(seasonings);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBaseRecipeId() {
        return baseRecipeId;
    }

    public List<String> getSeasonings() {
        return seasonings;
    }
}
