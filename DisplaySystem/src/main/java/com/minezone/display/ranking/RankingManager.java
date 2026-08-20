package com.minezone.display.ranking;

import com.minezone.display.DisplaySystemPlugin;
import com.minezone.display.config.DisplayConfig;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class RankingManager {
    private final DisplaySystemPlugin plugin;
    private final DisplayConfig config;
    private final Map<RankingType, RankingProvider> providers = new EnumMap<>(RankingType.class);
    private final Map<RankingType, RankingSnapshot> cache = new EnumMap<>(RankingType.class);
    private final Map<RankingType, Long> nextFullUpdate = new EnumMap<>(RankingType.class);
    private final Map<RankingType, Long> nextSupplementUpdate = new EnumMap<>(RankingType.class);
    private Consumer<RankingType> changeListener = type -> {};
    private BukkitTask task;

    public RankingManager(DisplaySystemPlugin plugin, DisplayConfig config, List<RankingProvider> rankingProviders) {
        this.plugin = plugin;
        this.config = config;
        for (RankingProvider provider : rankingProviders) {
            providers.put(provider.type(), provider);
            cache.put(provider.type(), RankingSnapshot.unavailable("&7Carregando ranking..."));
            nextFullUpdate.put(provider.type(), 0L);
            nextSupplementUpdate.put(provider.type(), 0L);
        }
    }

    public void setChangeListener(Consumer<RankingType> listener) {
        this.changeListener = listener == null ? type -> {} : listener;
    }

    public void start() {
        stop();
        forceRefresh();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void forceRefresh() {
        for (RankingType type : RankingType.values()) {
            nextFullUpdate.put(type, 0L);
            nextSupplementUpdate.put(type, 0L);
        }
        tick();
    }

    public RankingSnapshot snapshot(RankingType type) {
        return cache.getOrDefault(type, RankingSnapshot.unavailable("&cRanking indisponível."));
    }

    public RankingProvider provider(RankingType type) {
        return providers.get(type);
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (RankingType type : RankingType.values()) {
            RankingProvider provider = providers.get(type);
            if (provider == null) continue;

            if (now >= nextFullUpdate.getOrDefault(type, 0L)) {
                refreshFull(provider, now);
            }
            if (now >= nextSupplementUpdate.getOrDefault(type, 0L)) {
                refreshSupplement(provider, now);
            }
        }
    }

    private void refreshFull(RankingProvider provider, long now) {
        RankingType type = provider.type();
        nextFullUpdate.put(type, now + config.updateIntervalSeconds(type) * 1000L);
        RankingSnapshot previous = snapshot(type);
        RankingSnapshot updated;
        if (!provider.isAvailable()) {
            updated = RankingSnapshot.unavailable("&cSistema de ranking indisponível.");
        } else {
            try {
                List<RankingEntry> entries = provider.load(config.guiLimit(type));
                updated = new RankingSnapshot(true, entries, previous.supplement(), "");
            } catch (Exception ex) {
                plugin.getLogger().warning("Falha ao atualizar ranking " + type + ": " + ex.getMessage());
                updated = new RankingSnapshot(false, List.of(), previous.supplement(), "&cFalha ao carregar ranking.");
            }
        }
        putIfChanged(type, previous, updated);
    }

    private void refreshSupplement(RankingProvider provider, long now) {
        RankingType type = provider.type();
        nextSupplementUpdate.put(type, now + config.statusUpdateIntervalSeconds(type) * 1000L);
        RankingSnapshot previous = snapshot(type);
        if (!provider.isAvailable()) return;
        try {
            List<String> supplement = provider.loadSupplement();
            RankingSnapshot updated = previous.withSupplement(supplement);
            putIfChanged(type, previous, updated);
        } catch (Exception ex) {
            plugin.getLogger().fine("Falha ao atualizar complemento do ranking " + type + ": " + ex.getMessage());
        }
    }

    private void putIfChanged(RankingType type, RankingSnapshot previous, RankingSnapshot updated) {
        if (updated.equals(previous)) return;
        cache.put(type, updated);
        try {
            changeListener.accept(type);
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("Listener de ranking falhou para " + type + ": " + ex.getMessage());
        }
    }
}
