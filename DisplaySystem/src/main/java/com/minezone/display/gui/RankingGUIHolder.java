package com.minezone.display.gui;

import com.minezone.display.ranking.RankingType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class RankingGUIHolder implements InventoryHolder {
    private final RankingType type;
    private final int page;
    private Inventory inventory;

    public RankingGUIHolder(RankingType type, int page) {
        this.type = type;
        this.page = page;
    }

    public RankingType type() { return type; }
    public int page() { return page; }
    void bind(Inventory inventory) { this.inventory = inventory; }
    @Override public Inventory getInventory() { return inventory; }
}
