package com.minezone.display.ranking.provider;

import com.minezone.display.ranking.RankingEntry;
import com.minezone.display.ranking.RankingProvider;
import com.minezone.display.ranking.RankingType;
import com.minezone.display.util.FormatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EggRankingProvider implements RankingProvider {
    private Plugin cachedPlugin;
    private Object manager;
    private Method getTopPortadores;
    private Method getPortadorAtual;
    private Method getNomePortadorAtual;
    private Method getNomeUltimoPortador;
    private Method rankingUuid;
    private Method rankingNome;
    private Method rankingMillis;

    @Override public RankingType type() { return RankingType.OVO; }

    @Override public boolean isAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("EggSystem");
        if (plugin == null || !plugin.isEnabled()) return false;
        return ensureInitialized(plugin);
    }

    @Override public List<RankingEntry> load(int limit) throws Exception {
        if (!isAvailable()) return List.of();
        Object raw = getTopPortadores.invoke(manager, Math.max(1, limit));
        if (!(raw instanceof List<?> list)) return List.of();
        List<RankingEntry> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item == null) continue;
            UUID uuid = (UUID) rankingUuid.invoke(item);
            String name = String.valueOf(rankingNome.invoke(item));
            long millis = ((Number) rankingMillis.invoke(item)).longValue();
            String time = FormatUtil.duration(millis);
            result.add(new RankingEntry(uuid, name, "&f" + time,
                    List.of("&7Tempo acumulado: &f" + time), Material.PLAYER_HEAD));
        }
        return result;
    }

    @Override public List<String> loadSupplement() throws Exception {
        if (!isAvailable()) return List.of();
        Object current = getPortadorAtual.invoke(manager);
        boolean hasCurrent = current != null;
        if (current instanceof java.util.Optional<?> optional) hasCurrent = optional.isPresent();
        if (current instanceof String text) hasCurrent = !text.isBlank();
        if (hasCurrent) {
            Object name = getNomePortadorAtual.invoke(manager);
            return List.of("&8━━━━━━━━━━━━", "&a🟢 &7Portador atual:", "&a" + safeName(name));
        }
        Object last = getNomeUltimoPortador.invoke(manager);
        return List.of("&8━━━━━━━━━━━━", "&7⚪ Último portador:", "&f" + safeName(last));
    }

    private synchronized boolean ensureInitialized(Plugin plugin) {
        if (plugin == cachedPlugin && manager != null) return true;
        clear();
        try {
            Object resolvedManager = plugin.getClass().getMethod("getEggManager").invoke(plugin);
            if (resolvedManager == null) return false;
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> rankingClass = Class.forName("com.minezone.egg.EggManager$RankingPortador", true, loader);
            getTopPortadores = resolvedManager.getClass().getMethod("getTopPortadores", int.class);
            getPortadorAtual = resolvedManager.getClass().getMethod("getPortadorAtual");
            getNomePortadorAtual = resolvedManager.getClass().getMethod("getNomePortadorAtual");
            getNomeUltimoPortador = resolvedManager.getClass().getMethod("getNomeUltimoPortador");
            rankingUuid = rankingClass.getMethod("uuid");
            rankingNome = rankingClass.getMethod("nome");
            rankingMillis = rankingClass.getMethod("millis");
            manager = resolvedManager;
            cachedPlugin = plugin;
            return true;
        } catch (ReflectiveOperationException ex) {
            clear();
            return false;
        }
    }

    private static String safeName(Object value) {
        if (value == null) return "Nenhum";
        String name = value.toString().trim();
        return name.isBlank() ? "Nenhum" : name;
    }

    private void clear() {
        cachedPlugin = null;
        manager = null;
        getTopPortadores = null;
        getPortadorAtual = null;
        getNomePortadorAtual = null;
        getNomeUltimoPortador = null;
        rankingUuid = null;
        rankingNome = null;
        rankingMillis = null;
    }
}
