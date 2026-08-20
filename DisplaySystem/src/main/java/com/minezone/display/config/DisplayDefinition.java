package com.minezone.display.config;

import com.minezone.display.display.DisplayId;
import com.minezone.display.interaction.DisplayAction;
import org.bukkit.Material;

public record DisplayDefinition(
        DisplayId id,
        boolean enabled,
        DisplayAnchor anchor,
        String title,
        String subtitle,
        double displayOffsetY,
        double textOffsetY,
        float scale,
        String visualType,
        Material material,
        double interactionOffsetY,
        float interactionWidth,
        float interactionHeight,
        float rotationSpeedDegPerSecond,
        float bobHeight,
        float bobSpeed,
        int updateIntervalSeconds,
        int statusUpdateIntervalSeconds,
        int maxVisible,
        int guiLimit,
        DisplayAction action
) {}
