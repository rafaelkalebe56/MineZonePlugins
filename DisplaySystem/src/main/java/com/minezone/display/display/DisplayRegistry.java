package com.minezone.display.display;

import com.minezone.display.DisplaySystemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumMap;
import java.util.Map;

public final class DisplayRegistry {
    private final Map<DisplayId, DisplayBundle> bundles = new EnumMap<>(DisplayId.class);
    private final NamespacedKey managedKey;
    private final NamespacedKey idKey;
    private final NamespacedKey partKey;

    public DisplayRegistry(DisplaySystemPlugin plugin) {
        this.managedKey = new NamespacedKey(plugin, "managed");
        this.idKey = new NamespacedKey(plugin, "display_id");
        this.partKey = new NamespacedKey(plugin, "display_part");
    }

    public void register(DisplayBundle bundle) {
        DisplayBundle previous = bundles.put(bundle.id(), bundle);
        if (previous != null && previous != bundle) previous.remove();
    }

    public DisplayBundle get(DisplayId id) {
        return bundles.get(id);
    }

    public void remove(DisplayId id) {
        DisplayBundle bundle = bundles.remove(id);
        if (bundle != null) bundle.remove();
    }

    public void removeAll() {
        for (DisplayBundle bundle : bundles.values()) bundle.remove();
        bundles.clear();
    }

    public void cleanupOwnedEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (isOwned(entity)) entity.remove();
            }
        }
        bundles.clear();
    }

    public void mark(Entity entity, DisplayId id, String part) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(managedKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(idKey, PersistentDataType.STRING, id.commandKey());
        pdc.set(partKey, PersistentDataType.STRING, part == null ? "unknown" : part);
    }

    public boolean isOwned(Entity entity) {
        if (entity == null) return false;
        Byte value = entity.getPersistentDataContainer().get(managedKey, PersistentDataType.BYTE);
        return value != null && value == (byte) 1;
    }

    public DisplayId resolveId(Entity entity) {
        if (!isOwned(entity)) return null;
        String value = entity.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        return DisplayId.fromCommand(value);
    }

    public boolean isInteraction(Entity entity) {
        if (!isOwned(entity)) return false;
        String part = entity.getPersistentDataContainer().get(partKey, PersistentDataType.STRING);
        return "interaction".equalsIgnoreCase(part);
    }
}
