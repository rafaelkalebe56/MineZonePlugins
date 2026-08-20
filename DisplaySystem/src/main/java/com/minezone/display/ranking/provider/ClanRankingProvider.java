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

public final class ClanRankingProvider implements RankingProvider {
    private Plugin cachedPlugin;
    private Object manager;
    private Method ranking;
    private Method name;
    private Method tag;
    private Method level;
    private Method xp;
    private Method wins;
    private Method losses;
    private Method size;

    @Override public RankingType type() { return RankingType.CLAS; }

    @Override public boolean isAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ClaSystem");
        if (plugin == null || !plugin.isEnabled()) return false;
        return ensureInitialized(plugin);
    }

    @Override public List<RankingEntry> load(int limit) throws Exception {
        if (!isAvailable()) return List.of();
        Object raw = ranking.invoke(manager, Math.max(1, limit));
        if (!(raw instanceof List<?> list)) return List.of();
        List<RankingEntry> result = new ArrayList<>(list.size());
        for (Object clan : list) {
            if (clan == null) continue;
            String clanName = String.valueOf(name.invoke(clan));
            String clanTag = String.valueOf(tag.invoke(clan));
            int clanLevel = ((Number) level.invoke(clan)).intValue();
            int clanXp = ((Number) xp.invoke(clan)).intValue();
            int clanWins = ((Number) wins.invoke(clan)).intValue();
            int clanLosses = ((Number) losses.invoke(clan)).intValue();
            int members = ((Number) size.invoke(clan)).intValue();
            String value = "&7Nv.&f" + clanLevel + " &8• &b" + FormatUtil.integer(clanXp) + " XP &8• &a" + clanWins + "V";
            result.add(new RankingEntry(null, clanName, value, List.of(
                    "&7Tag: &b[" + clanTag + "]",
                    "&7Nível: &f" + clanLevel,
                    "&7XP: &b" + FormatUtil.integer(clanXp),
                    "&7Vitórias: &a" + clanWins,
                    "&7Derrotas: &c" + clanLosses,
                    "&7Membros: &f" + members
            ), Material.WHITE_BANNER));
        }
        return result;
    }

    private synchronized boolean ensureInitialized(Plugin plugin) {
        if (plugin == cachedPlugin && manager != null) return true;
        clear();
        try {
            Object resolvedManager = plugin.getClass().getMethod("getClanManager").invoke(plugin);
            if (resolvedManager == null) return false;
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> clanClass = Class.forName("com.minezone.cla.model.Clan", true, loader);
            ranking = resolvedManager.getClass().getMethod("ranking", int.class);
            name = clanClass.getMethod("name");
            tag = clanClass.getMethod("tag");
            level = clanClass.getMethod("level");
            xp = clanClass.getMethod("xp");
            wins = clanClass.getMethod("wins");
            losses = clanClass.getMethod("losses");
            size = clanClass.getMethod("size");
            manager = resolvedManager;
            cachedPlugin = plugin;
            return true;
        } catch (ReflectiveOperationException ex) {
            clear();
            return false;
        }
    }

    private void clear() {
        cachedPlugin = null;
        manager = null;
        ranking = null;
        name = null;
        tag = null;
        level = null;
        xp = null;
        wins = null;
        losses = null;
        size = null;
    }
}
