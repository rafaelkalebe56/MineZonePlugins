package com.minezone.display.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.UUID;

public final class AuthHook {
    private volatile Plugin cachedPlugin;
    private Object authManager;
    private MethodHandle getStateHandle;
    private volatile boolean initialized;

    public boolean isAvailable() {
        Plugin plugin = cachedPlugin;
        if (plugin == null || !plugin.isEnabled()) plugin = Bukkit.getPluginManager().getPlugin("AuthSystem");
        if (plugin == null || !plugin.isEnabled()) return false;
        return ensureInitialized(plugin);
    }

    public boolean isLogged(Player player) {
        Plugin plugin = cachedPlugin;
        if (plugin == null || !plugin.isEnabled()) plugin = Bukkit.getPluginManager().getPlugin("AuthSystem");
        if (plugin == null || !plugin.isEnabled()) return false;
        if (!ensureInitialized(plugin) || getStateHandle == null) return false;
        try {
            Object state = getStateHandle.invoke(player.getUniqueId());
            return state != null && "LOGGED".equalsIgnoreCase(state.toString());
        } catch (Throwable ignored) {
            return false;
        }
    }

    private synchronized boolean ensureInitialized(Plugin plugin) {
        if (initialized && cachedPlugin == plugin) return true;
        clearCache();
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            Method getAuthManager = plugin.getClass().getMethod("getAuthManager");
            authManager = getAuthManager.invoke(plugin);
            if (authManager == null) return false;
            getStateHandle = lookup.unreflect(authManager.getClass().getMethod("getState", UUID.class)).bindTo(authManager);
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
        authManager = null;
        getStateHandle = null;
    }
}
