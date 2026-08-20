package com.minezone.display.animation;

import com.minezone.display.DisplaySystemPlugin;
import com.minezone.display.config.DisplayConfig;
import com.minezone.display.config.DisplayDefinition;
import com.minezone.display.display.DisplayId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

public final class AnimationManager {
    private record AnimatedVisual(Display display, float scale, float rotationSpeed, float bobHeight, float bobSpeed) {}

    private final DisplaySystemPlugin plugin;
    private final DisplayConfig config;
    private final Map<DisplayId, AnimatedVisual> visuals = new EnumMap<>(DisplayId.class);
    private Runnable maintenance = () -> {};
    private BukkitTask task;
    private long startedAtNanos;
    private int iterations;

    public AnimationManager(DisplaySystemPlugin plugin, DisplayConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void setMaintenance(Runnable maintenance) {
        this.maintenance = maintenance == null ? () -> {} : maintenance;
    }

    public void register(DisplayDefinition definition, Display display) {
        if (definition == null || display == null || !definition.id().animated()) return;
        visuals.put(definition.id(), new AnimatedVisual(
                display,
                definition.scale(),
                definition.rotationSpeedDegPerSecond(),
                definition.bobHeight(),
                definition.bobSpeed()
        ));
    }

    public void unregister(DisplayId id) {
        visuals.remove(id);
    }

    public void clear() {
        visuals.clear();
    }

    public void start() {
        restart();
    }

    public void restart() {
        stopTaskOnly();
        startedAtNanos = System.nanoTime();
        iterations = 0;
        int interval = config.animationTickInterval();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, interval);
    }

    public void stop() {
        stopTaskOnly();
        visuals.clear();
    }

    private void stopTaskOnly() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        int interval = config.animationTickInterval();
        double elapsed = (System.nanoTime() - startedAtNanos) / 1_000_000_000D;
        double targetTime = elapsed + interval / 20D;

        for (AnimatedVisual visual : visuals.values()) {
            Display display = visual.display();
            if (display == null || !display.isValid()) continue;

            float angle = (float) Math.toRadians((visual.rotationSpeed() * targetTime) % 360D);
            float bob = visual.bobHeight() <= 0F || visual.bobSpeed() <= 0F
                    ? 0F
                    : (float) (Math.sin(targetTime * Math.PI * 2D * visual.bobSpeed()) * visual.bobHeight());

            display.setInterpolationDelay(0);
            display.setInterpolationDuration(interval);
            display.setTransformation(new Transformation(
                    new Vector3f(0F, bob, 0F),
                    new Quaternionf().rotationY(angle),
                    new Vector3f(visual.scale(), visual.scale(), visual.scale()),
                    new Quaternionf()
            ));
        }

        iterations++;
        int maintenanceEvery = Math.max(1, 20 / Math.max(1, interval));
        if (iterations % maintenanceEvery == 0) maintenance.run();
    }
}
