package com.minezone.display.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public final class RankHook {
    private Plugin cachedPlugin;
    private MethodHandle hasRankHandle;
    private boolean initialized;

    public boolean isAvailable() {
        Plugin plugin = cachedPlugin;
        if (plugin == null || !plugin.isEnabled()) plugin = Bukkit.getPluginManager().getPlugin("RankSystem");
        if (plugin == null || !plugin.isEnabled()) return false;
        return ensureInitialized(plugin);
    }

    public boolean ownsRank(Player player, String rank) {
        if (!isAvailable() || hasRankHandle == null) return false;
        try {
            return Boolean.TRUE.equals(hasRankHandle.invoke(player, rank));
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean isDisplayAdmin(Player player) {
        return ownsRank(player, "DONO") || ownsRank(player, "MOD");
    }

    private synchronized boolean ensureInitialized(Plugin plugin) {
        if (initialized && cachedPlugin == plugin) return true;
        clearCache();
        try {
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> api = Class.forName("com.minezone.rank.RankAPI", true, loader);
            Method hasRank = api.getMethod("hasRank", Player.class, String.class);
            hasRankHandle = MethodHandles.publicLookup().unreflect(hasRank);
            cachedPlugin = plugin;
            initialized = true;
            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            clearCache();
            return false;
        }
    }

    private void clearCache() {
        initialized = false;
        cachedPlugin = null;
        hasRankHandle = null;
    }
}
