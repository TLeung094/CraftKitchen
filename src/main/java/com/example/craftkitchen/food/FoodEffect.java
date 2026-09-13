package com.example.craftkitchen.food;

public class FoodEffect {
    private final String type;
    private final int duration;
    private final int amplifier;

    public FoodEffect(String type, int duration, int amplifier) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    public String getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }
}
