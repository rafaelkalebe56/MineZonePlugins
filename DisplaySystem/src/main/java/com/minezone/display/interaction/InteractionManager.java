package com.minezone.display.interaction;

import com.minezone.display.config.DisplayConfig;
import com.minezone.display.config.DisplayDefinition;
import com.minezone.display.display.DisplayId;
import com.minezone.display.display.DisplayRegistry;
import com.minezone.display.hook.AuthHook;
import com.minezone.display.util.TextUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InteractionManager implements Listener {
    private final DisplayConfig config;
    private final DisplayRegistry registry;
    private final AuthHook authHook;
    private final ActionExecutor actionExecutor;
    private final Map<UUID, Long> clickCooldown = new HashMap<>();

    public InteractionManager(DisplayConfig config, DisplayRegistry registry, AuthHook authHook, ActionExecutor actionExecutor) {
        this.config = config;
        this.registry = registry;
        this.authHook = authHook;
        this.actionExecutor = actionExecutor;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
        if (!registry.isInteraction(interaction)) return;
        event.setCancelled(true);
        handle(event.getPlayer(), interaction);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractAt(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;
        if (!registry.isInteraction(interaction)) return;
        event.setCancelled(true);
        handle(event.getPlayer(), interaction);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clickCooldown.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Interaction) || !registry.isInteraction(entity)) return;
        event.setCancelled(true);
        handle(player, entity);
    }

    private void handle(Player player, Entity entity) {
        DisplayId id = registry.resolveId(entity);
        if (id == null) return;

        if (config.requireAuth()) {
            if (!authHook.isAvailable()) {
                player.sendMessage(TextUtil.color("&cO AuthSystem está indisponível. Ação bloqueada por segurança."));
                return;
            }
            if (!authHook.isLogged(player)) {
                player.sendMessage(TextUtil.color("&cFaça login antes de usar os displays."));
                return;
            }
        }

        long now = System.currentTimeMillis();
        long last = clickCooldown.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < config.clickCooldownMillis()) return;
        clickCooldown.put(player.getUniqueId(), now);
        if (clickCooldown.size() > 2048) clickCooldown.entrySet().removeIf(entry -> now - entry.getValue() > 60_000L);

        DisplayDefinition definition = config.get(id);
        if (!definition.enabled()) return;
        actionExecutor.execute(player, definition.action());
    }
}
