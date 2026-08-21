package com.minezone.display.ranking;

import com.minezone.display.DisplaySystemPlugin;
import com.minezone.display.config.DisplayConfig;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class RankingManager {
    private final DisplaySystemPlugin plugin;
    private final DisplayConfig config;
    private final Map<RankingType, RankingProvider> providers = new EnumMap<>(RankingType.class);
    private final Map<RankingType, RankingSnapshot> cache = new EnumMap<>(RankingType.class);
    private final Map<RankingType, Long> nextFullUpdate = new EnumMap<>(RankingType.class);
    private final Map<RankingType, Long> nextSupplementUpdate = new EnumMap<>(RankingType.class);
    private final Set<RankingType> updatesInFlight = ConcurrentHashMap.newKeySet();
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
        updatesInFlight.clear();
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
        if (!provider.isAvailable()) {
            putIfChanged(type, previous, RankingSnapshot.unavailable("&cSistema de ranking indisponível."));
            return;
        }

        if (provider.loadsAsynchronously()) {
            if (!updatesInFlight.add(type)) return;
            int limit = config.guiLimit(type);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> loadAsync(provider, limit));
            return;
        }

        try {
            List<RankingEntry> entries = provider.load(config.guiLimit(type));
            RankingSnapshot current = snapshot(type);
            putIfChanged(type, current, new RankingSnapshot(true, entries, current.supplement(), ""));
        } catch (Exception ex) {
            plugin.getLogger().warning("Falha ao atualizar ranking " + type + ": " + ex.getMessage());
            RankingSnapshot current = snapshot(type);
            putIfChanged(type, current,
                    new RankingSnapshot(false, List.of(), current.supplement(), "&cFalha ao carregar ranking."));
        }
    }

    private void loadAsync(RankingProvider provider, int limit) {
        RankingType type = provider.type();
        List<RankingEntry> entries = List.of();
        Exception failure = null;
        try {
            entries = provider.load(limit);
        } catch (Exception ex) {
            failure = ex;
        }

        List<RankingEntry> loadedEntries = entries;
        Exception loadFailure = failure;
        if (!plugin.isEnabled()) {
            updatesInFlight.remove(type);
            return;
        }
        try {
            Bukkit.getScheduler().runTask(plugin, () -> {
                updatesInFlight.remove(type);
                RankingSnapshot current = snapshot(type);
                if (loadFailure == null) {
                    putIfChanged(type, current,
                            new RankingSnapshot(true, loadedEntries, current.supplement(), ""));
                } else {
                    plugin.getLogger().warning("Falha ao atualizar ranking " + type + ": " + loadFailure.getMessage());
                    putIfChanged(type, current,
                            new RankingSnapshot(false, List.of(), current.supplement(), "&cFalha ao carregar ranking."));
                }
            });
        } catch (RuntimeException ex) {
            updatesInFlight.remove(type);
            if (plugin.isEnabled()) throw ex;
        }
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
