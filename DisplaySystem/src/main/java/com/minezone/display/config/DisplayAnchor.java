package com.minezone.display.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public record DisplayAnchor(String world, int x, int y, int z) {
    public Location toCenteredLocation() {
        World resolved = Bukkit.getWorld(world);
        if (resolved == null) return null;
        return new Location(resolved, x + 0.5D, y, z + 0.5D);
    }
}
