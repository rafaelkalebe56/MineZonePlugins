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

public final class EconomyRankingProvider implements RankingProvider {
    private Plugin cachedPlugin;
    private Object manager;
    private Method getTopBalances;
    private Method getUuid;
    private Method getName;
    private Method getBalance;

    @Override public RankingType type() { return RankingType.DINHEIRO; }

    @Override public boolean isAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("EconomySystem");
        if (plugin == null || !plugin.isEnabled()) return false;
        return ensureInitialized(plugin);
    }

    @Override public List<RankingEntry> load(int limit) throws Exception {
        if (!isAvailable()) return List.of();
        Object raw = getTopBalances.invoke(manager, Math.max(1, limit));
        if (!(raw instanceof List<?> list)) return List.of();
        List<RankingEntry> result = new ArrayList<>(list.size());
        for (Object account : list) {
            if (account == null) continue;
            UUID uuid = (UUID) getUuid.invoke(account);
            String name = String.valueOf(getName.invoke(account));
            double balance = ((Number) getBalance.invoke(account)).doubleValue();
            String formatted = FormatUtil.money(balance);
            result.add(new RankingEntry(uuid, name, "&a" + formatted,
                    List.of("&7Saldo: &a" + formatted), Material.PLAYER_HEAD));
        }
        return result;
    }

    private synchronized boolean ensureInitialized(Plugin plugin) {
        if (plugin == cachedPlugin && manager != null) return true;
        clear();
        try {
            Object resolvedManager = plugin.getClass().getMethod("getEconomyManager").invoke(plugin);
            if (resolvedManager == null) return false;
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> accountClass = Class.forName("com.minezone.economy.model.PlayerAccount", true, loader);
            getTopBalances = resolvedManager.getClass().getMethod("getTopBalances", int.class);
            getUuid = accountClass.getMethod("getUuid");
            getName = accountClass.getMethod("getName");
            getBalance = accountClass.getMethod("getBalance");
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
        getTopBalances = null;
        getUuid = null;
        getName = null;
        getBalance = null;
    }
}
