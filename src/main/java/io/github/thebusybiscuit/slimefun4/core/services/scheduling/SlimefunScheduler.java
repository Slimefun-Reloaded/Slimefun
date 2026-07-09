package io.github.thebusybiscuit.slimefun4.core.services.scheduling;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

/**
 * Centralized Paper scheduler facade for Slimefun.
 * <p>
 * Folia does not have one synchronous "server thread". Pick a scheduler by data
 * ownership: global for global state, region for blocks/locations, entity for
 * players and other entities, async for server-independent work.
 */
@ParametersAreNonnullByDefault
public final class SlimefunScheduler {

    private static final long TICK_MILLIS = 50L;

    private final Slimefun plugin;
    private final boolean folia;
    private final Set<ScheduledTask> tasks = ConcurrentHashMap.newKeySet();

    public SlimefunScheduler(Slimefun plugin) {
        this.plugin = plugin;
        this.folia = isFoliaServer();
    }

    public boolean isFolia() {
        return folia;
    }

    public boolean isOwnedByCurrentRegion(Location location) {
        Validate.notNull(location, "Cannot check ownership without a location");

        if (isUnitTest()) {
            return true;
        }

        return plugin.getServer().isOwnedByCurrentRegion(location);
    }

    public boolean isOwnedByCurrentRegion(Block block) {
        Validate.notNull(block, "Cannot check ownership without a block");

        if (isUnitTest()) {
            return true;
        }

        return plugin.getServer().isOwnedByCurrentRegion(block);
    }

    public boolean isOwnedByCurrentRegion(Entity entity) {
        Validate.notNull(entity, "Cannot check ownership without an entity");

        if (isUnitTest()) {
            return true;
        }

        return plugin.getServer().isOwnedByCurrentRegion(entity);
    }

    public boolean isServerTickThread() {
        if (isUnitTest()) {
            return false;
        }

        return plugin.getServer().isPrimaryThread() || plugin.getServer().isGlobalTickThread();
    }

    public @Nullable ScheduledTask runGlobal(Runnable runnable) {
        Validate.notNull(runnable, "Cannot run null");

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getGlobalRegionScheduler().run(plugin, oneShot(runnable)));
    }

    public @Nullable ScheduledTask runGlobalLater(Runnable runnable, long delay) {
        Validate.notNull(runnable, "Cannot run null");
        Validate.isTrue(delay >= 0, "The delay cannot be negative");

        if (delay == 0) {
            return runGlobal(runnable);
        }

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, oneShot(runnable), delay));
    }

    public @Nullable ScheduledTask runGlobalTimer(Runnable runnable, long delay, long period) {
        Validate.notNull(runnable, "Cannot run null");
        Validate.isTrue(delay >= 0, "The delay cannot be negative");
        Validate.isTrue(period > 0, "The period must be positive");

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, repeating(runnable), Math.max(1L, delay), period));
    }

    public @Nullable ScheduledTask runRegion(Location location, Runnable runnable) {
        Validate.notNull(location, "Cannot run without a location");
        Validate.notNull(runnable, "Cannot run null");

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getRegionScheduler().run(plugin, location, oneShot(runnable)));
    }

    public @Nullable ScheduledTask runRegionLater(Location location, Runnable runnable, long delay) {
        Validate.notNull(location, "Cannot run without a location");
        Validate.notNull(runnable, "Cannot run null");
        Validate.isTrue(delay >= 0, "The delay cannot be negative");

        if (delay == 0) {
            return runRegion(location, runnable);
        }

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getRegionScheduler().runDelayed(plugin, location, oneShot(runnable), delay));
    }

    public @Nullable ScheduledTask runRegionTimer(Location location, Runnable runnable, long delay, long period) {
        Validate.notNull(location, "Cannot run without a location");
        Validate.notNull(runnable, "Cannot run null");
        Validate.isTrue(delay >= 0, "The delay cannot be negative");
        Validate.isTrue(period > 0, "The period must be positive");

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, location, repeating(runnable), Math.max(1L, delay), period));
    }

    public @Nullable ScheduledTask runEntity(Entity entity, Runnable runnable) {
        Validate.notNull(entity, "Cannot run without an entity");
        Validate.notNull(runnable, "Cannot run null");

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(entity.getScheduler().run(plugin, oneShot(runnable), null));
    }

    public @Nullable ScheduledTask runEntityLater(Entity entity, Runnable runnable, long delay) {
        Validate.notNull(entity, "Cannot run without an entity");
        Validate.notNull(runnable, "Cannot run null");
        Validate.isTrue(delay >= 0, "The delay cannot be negative");

        if (delay == 0) {
            return runEntity(entity, runnable);
        }

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(entity.getScheduler().runDelayed(plugin, oneShot(runnable), null, delay));
    }

    public @Nullable ScheduledTask runEntityTimer(Entity entity, Runnable runnable, long delay, long period) {
        Validate.notNull(entity, "Cannot run without an entity");
        Validate.notNull(runnable, "Cannot run null");
        Validate.isTrue(delay >= 0, "The delay cannot be negative");
        Validate.isTrue(period > 0, "The period must be positive");

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(entity.getScheduler().runAtFixedRate(plugin, repeating(runnable), null, Math.max(1L, delay), period));
    }

    public @Nullable ScheduledTask runAsync(Runnable runnable) {
        Validate.notNull(runnable, "Cannot run null");

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getAsyncScheduler().runNow(plugin, oneShot(runnable)));
    }

    public @Nullable ScheduledTask runAsyncLater(Runnable runnable, long delay) {
        Validate.notNull(runnable, "Cannot run null");
        Validate.isTrue(delay >= 0, "The delay cannot be negative");

        if (delay == 0) {
            return runAsync(runnable);
        }

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getAsyncScheduler().runDelayed(plugin, oneShot(runnable), ticksToMillis(delay), TimeUnit.MILLISECONDS));
    }

    public @Nullable ScheduledTask runAsyncTimer(Runnable runnable, long delay, long period) {
        Validate.notNull(runnable, "Cannot run null");
        Validate.isTrue(delay >= 0, "The delay cannot be negative");
        Validate.isTrue(period > 0, "The period must be positive");

        if (isUnitTest()) {
            runnable.run();
            return null;
        }

        return track(plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, repeating(runnable), ticksToMillis(delay), ticksToMillis(period), TimeUnit.MILLISECONDS));
    }

    public void cancelAll() {
        for (ScheduledTask task : tasks) {
            task.cancel();
        }

        tasks.clear();
    }

    private boolean isUnitTest() {
        return Slimefun.getMinecraftVersion() == MinecraftVersion.UNIT_TEST;
    }

    private @Nonnull Consumer<ScheduledTask> oneShot(Runnable runnable) {
        return task -> {
            try {
                runnable.run();
            } finally {
                tasks.remove(task);
            }
        };
    }

    private @Nonnull Consumer<ScheduledTask> repeating(Runnable runnable) {
        return task -> runnable.run();
    }

    private @Nonnull ScheduledTask track(ScheduledTask task) {
        tasks.add(task);

        if (isTerminal(task)) {
            tasks.remove(task);
        }

        return task;
    }

    private boolean isTerminal(ScheduledTask task) {
        ScheduledTask.ExecutionState state = task.getExecutionState();

        return state == ScheduledTask.ExecutionState.FINISHED
            || state == ScheduledTask.ExecutionState.CANCELLED
            || state == ScheduledTask.ExecutionState.CANCELLED_RUNNING;
    }

    private long ticksToMillis(long ticks) {
        return Math.max(0L, ticks) * TICK_MILLIS;
    }

    private static boolean isFoliaServer() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException x) {
            return false;
        }
    }
}
