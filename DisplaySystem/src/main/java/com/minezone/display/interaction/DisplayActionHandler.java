package com.minezone.display.interaction;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface DisplayActionHandler {
    void execute(Player player, String value);
}
