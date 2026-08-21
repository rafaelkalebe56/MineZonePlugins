package com.minezone.display.config;

import com.minezone.display.DisplaySystemPlugin;
import com.minezone.display.display.DisplayId;
import com.minezone.display.interaction.DisplayAction;
import com.minezone.display.interaction.DisplayActionType;
import com.minezone.display.ranking.RankingType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

public final class DisplayConfig {
    private record DefaultAnchor(String world, int x, int y, int z) {}

    private static final Map<DisplayId, DefaultAnchor> DEFAULT_ANCHORS = new EnumMap<>(DisplayId.class);
    static {
        DEFAULT_ANCHORS.put(DisplayId.RTP, new DefaultAnchor("lobby", 4, 65, 6));
        DEFAULT_ANCHORS.put(DisplayId.CONTINUAR, new DefaultAnchor("lobby", -4, 65, 6));
        DEFAULT_ANCHORS.put(DisplayId.EVENTOS, new DefaultAnchor("lobby", 0, 65, 6));
        DEFAULT_ANCHORS.put(DisplayId.TOP_OVO, new DefaultAnchor("lobby", 3, 65, 10));
        DEFAULT_ANCHORS.put(DisplayId.TOP_DINHEIRO, new DefaultAnchor("lobby", 0, 65, 10));
        DEFAULT_ANCHORS.put(DisplayId.TOP_CLAS, new DefaultAnchor("lobby", -3, 65, 10));
    }

    private final DisplaySystemPlugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public DisplayConfig(DisplaySystemPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "displays.yml");
    }

    public synchronized void reload() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Não foi possível criar a pasta do DisplaySystem.");
        }
        if (!file.exists()) plugin.saveResource("displays.yml", false);

        yaml = YamlConfiguration.loadConfiguration(file);
        try (var stream = plugin.getResource("displays.yml")) {
            if (stream != null) {
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                yaml.setDefaults(defaults);
                yaml.options().copyDefaults(true);
                yaml.save(file);
            }
        } catch (IOException ex) {
            plugin.getLogger().warning("Não foi possível completar displays.yml com os padrões: " + ex.getMessage());
        }
    }

    public synchronized DisplayDefinition get(DisplayId id) {
        ensureLoaded();
        String path = id.configPath();
        DefaultAnchor fallback = DEFAULT_ANCHORS.get(id);
        String fallbackWorld = yaml.getString("lobby.world", fallback.world());
        DisplayAnchor anchor = new DisplayAnchor(
                yaml.getString(path + ".anchor.world", fallbackWorld),
                yaml.getInt(path + ".anchor.x", fallback.x()),
                yaml.getInt(path + ".anchor.y", fallback.y()),
                yaml.getInt(path + ".anchor.z", fallback.z())
        );

        String defaultTitle = switch (id) {
            case RTP -> "&b&lRTP";
            case CONTINUAR -> "&d&lCONTINUAR";
            case EVENTOS -> "&6&l⚡ EVENTOS";
            case TOP_OVO -> "&6&l🥚 TOP OVO";
            case TOP_DINHEIRO -> "&e&l💰 TOP DINHEIRO";
            case TOP_CLAS -> "&b&l🛡 TOP CLÃS";
        };
        String defaultSubtitle = switch (id) {
            case RTP -> "&7Clique para viajar";
            case CONTINUAR -> "&7Volte para sua aventura";
            case EVENTOS -> "&7Clique para ver os eventos";
            case TOP_OVO -> "&7Mais tempo com o Ovo do Dragão";
            case TOP_DINHEIRO -> "&7Jogadores mais ricos";
            case TOP_CLAS -> "&7Clãs mais bem colocados";
        };

        Material defaultMaterial = id == DisplayId.RTP ? Material.ENDER_PEARL
                : id == DisplayId.CONTINUAR ? Material.COMPASS
                : id == DisplayId.EVENTOS ? Material.CLOCK : Material.AIR;
        Material material = Material.matchMaterial(yaml.getString(path + ".material", defaultMaterial.name()));
        if (material == null) material = defaultMaterial;

        String defaultActionType = id.isRanking() ? "RANKING" : "COMMAND";
        String defaultActionValue = switch (id) {
            case RTP -> "rtp";
            case CONTINUAR -> "continuar";
            case EVENTOS -> "evento";
            case TOP_OVO -> "ovo";
            case TOP_DINHEIRO -> "dinheiro";
            case TOP_CLAS -> "clas";
        };

        return new DisplayDefinition(
                id,
                yaml.getBoolean(path + ".enabled", true),
                anchor,
                yaml.getString(path + ".title", defaultTitle),
                yaml.getString(path + ".subtitle", defaultSubtitle),
                yaml.getDouble(path + ".display-offset-y", 1.35D),
                yaml.getDouble(path + ".text-offset-y", id.isRanking() ? 1.65D : 2.45D),
                clampFloat((float) yaml.getDouble(path + ".scale", id.isRanking() ? 0.90D : 0.85D), 0.10F, 5.0F),
                yaml.getString(path + ".visual-type", "ITEM"),
                material,
                yaml.getDouble(path + ".interaction-offset-y", id.isRanking() ? 2.5D : 1.8D),
                clampFloat((float) yaml.getDouble(path + ".interaction-width", id.isRanking() ? 3.2D : 1.6D), 0.1F, 32F),
                clampFloat((float) yaml.getDouble(path + ".interaction-height", id.isRanking() ? 3.6D : 2.6D), 0.1F, 32F),
                clampFloat((float) yaml.getDouble(path + ".rotation-speed-deg-per-second", 50D), -720F, 720F),
                clampFloat((float) yaml.getDouble(path + ".bob-height", 0.15D), 0F, 4F),
                clampFloat((float) yaml.getDouble(path + ".bob-speed", 0.5D), 0F, 10F),
                Math.max(5, yaml.getInt(path + ".update-interval-seconds", 60)),
                Math.max(1, yaml.getInt(path + ".status-update-interval-seconds",
                        id == DisplayId.EVENTOS ? 1 : id == DisplayId.TOP_OVO ? 5 : 60)),
                Math.max(1, Math.min(20, yaml.getInt(path + ".max-visible", 5))),
                Math.max(10, Math.min(1000, yaml.getInt(path + ".gui-limit", 100))),
                new DisplayAction(
                        DisplayActionType.from(yaml.getString(path + ".action.type", defaultActionType)),
                        yaml.getString(path + ".action.value", defaultActionValue)
                )
        );
    }

    public synchronized void setAnchor(DisplayId id, Location location) {
        ensureLoaded();
        if (location == null || location.getWorld() == null) return;
        String path = id.configPath() + ".anchor";
        yaml.set(path + ".world", location.getWorld().getName());
        yaml.set(path + ".x", location.getBlockX());
        yaml.set(path + ".y", location.getBlockY());
        yaml.set(path + ".z", location.getBlockZ());
        save();
    }

    public synchronized void resetAnchor(DisplayId id) {
        ensureLoaded();
        DefaultAnchor anchor = DEFAULT_ANCHORS.get(id);
        String path = id.configPath() + ".anchor";
        yaml.set(path + ".world", null);
        yaml.set(path + ".x", anchor.x());
        yaml.set(path + ".y", anchor.y());
        yaml.set(path + ".z", anchor.z());
        save();
    }

    public boolean requireAuth() {
        ensureLoaded();
        return yaml.getBoolean("lobby.require-auth", true);
    }

    public long clickCooldownMillis() {
        ensureLoaded();
        return Math.max(100L, yaml.getLong("lobby.click-cooldown-ms", 500L));
    }

    public int animationTickInterval() {
        ensureLoaded();
        return Math.max(1, Math.min(20, yaml.getInt("animation.tick-interval", 5)));
    }

    public int guiLimit(RankingType type) {
        DisplayId id = displayFor(type);
        return id == null ? 100 : get(id).guiLimit();
    }

    public int maxVisible(RankingType type) {
        DisplayId id = displayFor(type);
        return id == null ? 5 : get(id).maxVisible();
    }

    public int updateIntervalSeconds(RankingType type) {
        DisplayId id = displayFor(type);
        return id == null ? 60 : get(id).updateIntervalSeconds();
    }

    public int statusUpdateIntervalSeconds(RankingType type) {
        DisplayId id = displayFor(type);
        return id == null ? 60 : get(id).statusUpdateIntervalSeconds();
    }

    private DisplayId displayFor(RankingType type) {
        if (type == null) return null;
        return switch (type) {
            case OVO -> DisplayId.TOP_OVO;
            case DINHEIRO -> DisplayId.TOP_DINHEIRO;
            case CLAS -> DisplayId.TOP_CLAS;
        };
    }

    private void save() {
        try {
            yaml.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Falha ao salvar displays.yml: " + ex.getMessage());
        }
    }

    private void ensureLoaded() {
        if (yaml == null) reload();
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
