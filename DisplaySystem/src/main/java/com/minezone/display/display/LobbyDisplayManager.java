package com.minezone.display.display;

import com.minezone.display.DisplaySystemPlugin;
import com.minezone.display.animation.AnimationManager;
import com.minezone.display.config.DisplayConfig;
import com.minezone.display.config.DisplayDefinition;
import com.minezone.display.event.EventDisplayText;
import com.minezone.display.hook.EventHook;
import com.minezone.display.ranking.RankingEntry;
import com.minezone.display.ranking.RankingManager;
import com.minezone.display.ranking.RankingSnapshot;
import com.minezone.display.ranking.RankingType;
import com.minezone.display.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LobbyDisplayManager {
    private final DisplaySystemPlugin plugin;
    private final DisplayConfig config;
    private final DisplayRegistry registry;
    private final RankingManager rankingManager;
    private final AnimationManager animationManager;
    private final EventHook eventHook;
    private final Set<DisplayId> warnedMissingWorld = EnumSet.noneOf(DisplayId.class);
    private long nextEventStatusUpdate;

    public LobbyDisplayManager(DisplaySystemPlugin plugin,
                               DisplayConfig config,
                               DisplayRegistry registry,
                               RankingManager rankingManager,
                               AnimationManager animationManager,
                               EventHook eventHook) {
        this.plugin = plugin;
        this.config = config;
        this.registry = registry;
        this.rankingManager = rankingManager;
        this.animationManager = animationManager;
        this.eventHook = eventHook;
    }

    public void start() {
        registry.cleanupOwnedEntities();
        rebuildAll();
        animationManager.setMaintenance(this::maintenanceTick);
        animationManager.start();
    }

    public void stop() {
        animationManager.stop();
        registry.removeAll();
        registry.cleanupOwnedEntities();
    }

    public void reloadAll() {
        animationManager.clear();
        registry.removeAll();
        registry.cleanupOwnedEntities();
        warnedMissingWorld.clear();
        nextEventStatusUpdate = 0L;
        rebuildAll();
        animationManager.restart();
    }

    public void rebuildAll() {
        for (DisplayId id : DisplayId.values()) rebuild(id);
    }

    public void rebuild(DisplayId id) {
        if (id == null) return;
        animationManager.unregister(id);
        registry.remove(id);

        DisplayDefinition definition = config.get(id);
        if (!definition.enabled()) return;

        Location anchor = definition.anchor().toCenteredLocation();
        if (anchor == null || anchor.getWorld() == null) {
            if (warnedMissingWorld.add(id)) {
                plugin.getLogger().warning("Display " + id.commandKey() + " não foi criado: mundo '"
                        + definition.anchor().world() + "' não está carregado.");
            }
            return;
        }
        warnedMissingWorld.remove(id);

        Display visual = id.animated() ? spawnVisual(definition, anchor) : null;
        TextDisplay text = spawnText(definition, anchor);
        Interaction interaction = spawnInteraction(definition, anchor);
        DisplayBundle bundle = new DisplayBundle(id, visual, text, interaction);
        registry.register(bundle);

        if (id == DisplayId.EVENTOS) updateEventStatus(true);
        else if (id.isRanking()) updateRanking(id.rankingType());
        else {
            String raw = staticText(definition);
            bundle.updateText(raw, TextUtil.component(raw));
        }

        if (visual != null && id.animated()) animationManager.register(definition, visual);
    }

    public void updateRanking(RankingType type) {
        DisplayId id = displayFor(type);
        if (id == null) return;
        DisplayBundle bundle = registry.get(id);
        if (bundle == null || !bundle.isValid()) return;
        String raw = rankingText(config.get(id), rankingManager.snapshot(type));
        bundle.updateText(raw, TextUtil.component(raw));
    }

    public DisplayRegistry registry() {
        return registry;
    }

    private Display spawnVisual(DisplayDefinition definition, Location anchor) {
        World world = anchor.getWorld();
        if (world == null) return null;
        Location location = anchor.clone().add(0D, definition.displayOffsetY(), 0D);
        String visualType = definition.visualType() == null ? "ITEM" : definition.visualType().toUpperCase(Locale.ROOT);
        Display display;

        if ("BLOCK".equals(visualType) && definition.material().isBlock()) {
            BlockDisplay block = world.spawn(location, BlockDisplay.class);
            block.setBlock(definition.material().createBlockData());
            display = block;
        } else {
            Material material = definition.material().isAir() ? Material.STONE : definition.material();
            ItemDisplay item = world.spawn(location, ItemDisplay.class);
            item.setItemStack(new ItemStack(material));
            item.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display = item;
        }

        configureDisplay(display);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(config.animationTickInterval());
        float scale = definition.scale();
        display.setTransformation(new Transformation(
                new Vector3f(), new Quaternionf(),
                new Vector3f(scale, scale, scale), new Quaternionf()));
        registry.mark(display, definition.id(), "visual");
        return display;
    }

    private TextDisplay spawnText(DisplayDefinition definition, Location anchor) {
        World world = anchor.getWorld();
        Location location = anchor.clone().add(0D, definition.textOffsetY(), 0D);
        TextDisplay text = world.spawn(location, TextDisplay.class);
        configureDisplay(text);
        text.setBillboard(Display.Billboard.CENTER);
        text.setAlignment(TextDisplay.TextAlignment.CENTER);
        text.setLineWidth(definition.id().isRanking() ? 420 : 280);
        text.setShadowed(true);
        text.setSeeThrough(false);
        text.setDefaultBackground(false);
        float scale = definition.scale();
        text.setTransformation(new Transformation(
                new Vector3f(), new Quaternionf(),
                new Vector3f(scale, scale, scale), new Quaternionf()));
        registry.mark(text, definition.id(), "text");
        return text;
    }

    private Interaction spawnInteraction(DisplayDefinition definition, Location anchor) {
        World world = anchor.getWorld();
        Location location = anchor.clone().add(0D, definition.interactionOffsetY(), 0D);
        Interaction interaction = world.spawn(location, Interaction.class);
        interaction.setInteractionWidth(definition.interactionWidth());
        interaction.setInteractionHeight(definition.interactionHeight());
        interaction.setResponsive(true);
        interaction.setPersistent(true);
        interaction.setInvulnerable(true);
        interaction.setGravity(false);
        interaction.setSilent(true);
        registry.mark(interaction, definition.id(), "interaction");
        return interaction;
    }

    private void configureDisplay(Display display) {
        display.setPersistent(true);
        display.setInvulnerable(true);
        display.setGravity(false);
        display.setSilent(true);
        display.setViewRange(32F);
        display.setTeleportDuration(0);
    }

    private void maintenanceTick() {
        for (DisplayId id : DisplayId.values()) {
            DisplayDefinition definition = config.get(id);
            DisplayBundle bundle = registry.get(id);
            if (!definition.enabled()) {
                if (bundle != null) {
                    animationManager.unregister(id);
                    registry.remove(id);
                }
                continue;
            }
            if (bundle == null || !bundle.isValid()) rebuild(id);
        }
        updateEventStatus(false);
    }

    private void updateEventStatus(boolean force) {
        long now = System.currentTimeMillis();
        if (!force && now < nextEventStatusUpdate) return;

        DisplayDefinition definition = config.get(DisplayId.EVENTOS);
        nextEventStatusUpdate = now + definition.statusUpdateIntervalSeconds() * 1000L;
        if (!definition.enabled()) return;

        DisplayBundle bundle = registry.get(DisplayId.EVENTOS);
        if (bundle == null || !bundle.isValid()) return;
        String raw = EventDisplayText.format(definition.title(), definition.subtitle(), eventHook.snapshot());
        bundle.updateText(raw, TextUtil.component(raw));
    }

    private String staticText(DisplayDefinition definition) {
        String title = nullToEmpty(definition.title());
        String subtitle = nullToEmpty(definition.subtitle());
        if (subtitle.isBlank()) return title;
        return title + "\n" + subtitle;
    }

    private String rankingText(DisplayDefinition definition, RankingSnapshot snapshot) {
        StringBuilder out = new StringBuilder();
        out.append(nullToEmpty(definition.title()));
        if (!nullToEmpty(definition.subtitle()).isBlank()) out.append('\n').append(definition.subtitle());
        out.append("\n");

        if (!snapshot.available()) {
            out.append('\n').append(snapshot.statusMessage().isBlank() ? "&cRanking indisponível." : snapshot.statusMessage());
        } else {
            List<RankingEntry> entries = snapshot.entries();
            int visible = Math.min(Math.min(definition.maxVisible(), entries.size()), 20);
            if (visible == 0) {
                out.append("\n&7Nenhum dado no ranking.");
            } else {
                for (int i = 0; i < visible; i++) {
                    RankingEntry entry = entries.get(i);
                    out.append('\n').append(positionPrefix(i + 1)).append(' ')
                            .append("&f").append(entry.name());
                    if (!entry.valueText().isBlank()) out.append("  ").append(entry.valueText());
                }
            }
        }

        if (!snapshot.supplement().isEmpty()) {
            for (String line : snapshot.supplement()) out.append('\n').append(line);
        }
        return out.toString();
    }

    private static String positionPrefix(int position) {
        return switch (position) {
            case 1 -> "&6🥇";
            case 2 -> "&7🥈";
            case 3 -> "&c🥉";
            default -> "&7#" + position;
        };
    }

    private static DisplayId displayFor(RankingType type) {
        if (type == null) return null;
        return switch (type) {
            case OVO -> DisplayId.TOP_OVO;
            case DINHEIRO -> DisplayId.TOP_DINHEIRO;
            case CLAS -> DisplayId.TOP_CLAS;
        };
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
