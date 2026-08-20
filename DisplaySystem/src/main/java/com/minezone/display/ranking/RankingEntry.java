package com.minezone.display.ranking;

import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

public record RankingEntry(
        UUID profileUuid,
        String name,
        String valueText,
        List<String> lore,
        Material icon
) {
    public RankingEntry {
        if (name == null || name.isBlank()) name = "Desconhecido";
        if (valueText == null) valueText = "";
        lore = lore == null ? List.of() : List.copyOf(lore);
        if (icon == null) icon = Material.PAPER;
    }
}
