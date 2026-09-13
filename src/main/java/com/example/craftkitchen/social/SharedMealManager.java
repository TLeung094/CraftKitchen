package com.example.craftkitchen.social;

import org.bukkit.entity.Player;

public class SharedMealManager {
    private final double radius;
    private final double multiplier;

    public SharedMealManager(double radius, double multiplier) {
        this.radius = radius;
        this.multiplier = multiplier;
    }

    public boolean isWithinRadius(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }

    public int scaleDuration(int durationSeconds, boolean sharing) {
        if (!sharing) {
            return durationSeconds;
        }
        return (int) Math.round(durationSeconds * multiplier);
    }

    public boolean hasNearbyPlayers(Player player) {
        return player.getWorld().getPlayers().stream()
            .filter(other -> !other.getUniqueId().equals(player.getUniqueId()))
            .anyMatch(other -> isWithinRadius(
                player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(),
                other.getLocation().getX(), other.getLocation().getY(), other.getLocation().getZ()
            ));
    }

    public double getRadius() {
        return radius;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
