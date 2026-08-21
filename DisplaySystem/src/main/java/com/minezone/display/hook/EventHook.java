package com.minezone.display.hook;

import com.minezone.display.event.EventStatusSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public final class EventHook {
    private Plugin cachedPlugin;
    private Object manager;
    private Method getActive;
    private Method getSetup;
    private Method getState;
    private Method getSignupRemaining;
    private Method getPreparationRemaining;
    private Method participantCount;
    private Method aliveCount;
    private Method spectatorCount;
    private Method isPvpEnabled;
    private Method getType;
    private Method typeDisplay;

    public EventStatusSnapshot snapshot() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("EventSystem");
        if (plugin == null || !plugin.isEnabled() || !ensureInitialized(plugin)) {
            return EventStatusSnapshot.unavailable();
        }

        try {
            Object session = getActive.invoke(manager);
            if (session == null) return EventStatusSnapshot.idle();

            Object setup = getSetup.invoke(session);
            Object type = getType.invoke(setup);
            Object stateValue = getState.invoke(session);
            String state = stateValue == null ? "" : stateValue.toString();
            int countdown = switch (state) {
                case "WAITING" -> number(getSignupRemaining.invoke(session));
                case "PREPARING" -> number(getPreparationRemaining.invoke(session));
                default -> 0;
            };

            return new EventStatusSnapshot(
                    true,
                    true,
                    type == null ? "Evento" : String.valueOf(typeDisplay.invoke(type)),
                    state,
                    countdown,
                    number(participantCount.invoke(session)),
                    number(aliveCount.invoke(session)),
                    number(spectatorCount.invoke(session)),
                    Boolean.TRUE.equals(isPvpEnabled.invoke(session))
            );
        } catch (ReflectiveOperationException | RuntimeException ex) {
            clear();
            return EventStatusSnapshot.unavailable();
        }
    }

    private synchronized boolean ensureInitialized(Plugin plugin) {
        if (plugin == cachedPlugin && manager != null) return true;
        clear();
        try {
            Object resolvedManager = plugin.getClass().getMethod("getEventManager").invoke(plugin);
            if (resolvedManager == null) return false;
            ClassLoader loader = plugin.getClass().getClassLoader();
            Class<?> sessionClass = Class.forName("com.minezone.event.model.EventSession", true, loader);
            Class<?> setupClass = Class.forName("com.minezone.event.model.EventSetup", true, loader);
            Class<?> typeClass = Class.forName("com.minezone.event.model.EventType", true, loader);

            getActive = resolvedManager.getClass().getMethod("getActive");
            getSetup = sessionClass.getMethod("getSetup");
            getState = sessionClass.getMethod("getState");
            getSignupRemaining = sessionClass.getMethod("getSignupRemaining");
            getPreparationRemaining = sessionClass.getMethod("getPreparationRemaining");
            participantCount = sessionClass.getMethod("participantCount");
            aliveCount = sessionClass.getMethod("aliveCount");
            spectatorCount = sessionClass.getMethod("spectatorCount");
            isPvpEnabled = sessionClass.getMethod("isPvpEnabled");
            getType = setupClass.getMethod("getType");
            typeDisplay = typeClass.getMethod("display");
            manager = resolvedManager;
            cachedPlugin = plugin;
            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            clear();
            return false;
        }
    }

    private static int number(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private void clear() {
        cachedPlugin = null;
        manager = null;
        getActive = null;
        getSetup = null;
        getState = null;
        getSignupRemaining = null;
        getPreparationRemaining = null;
        participantCount = null;
        aliveCount = null;
        spectatorCount = null;
        isPvpEnabled = null;
        getType = null;
        typeDisplay = null;
    }
}
