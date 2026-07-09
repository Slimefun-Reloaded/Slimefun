package io.github.thebusybiscuit.slimefun4.implementation.tasks.player;

import javax.annotation.Nonnull;

import org.bukkit.entity.Player;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

abstract class AbstractPlayerTask implements Runnable {

    protected final Player p;
    private ScheduledTask task;

    AbstractPlayerTask(@Nonnull Player p) {
        this.p = p;
    }

    private void setTask(ScheduledTask task) {
        this.task = task;
    }

    public void schedule(long delay) {
        setTask(Slimefun.getScheduler().runEntityLater(p, this, delay));
    }

    public void scheduleRepeating(long delay, long interval) {
        setTask(Slimefun.getScheduler().runEntityTimer(p, this, delay, interval));
    }

    @Override
    public final void run() {
        if (isValid()) {
            executeTask();
        }
    }

    /**
     * This method cancels this {@link AbstractPlayerTask}.
     */
    public final void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * This method checks if this {@link AbstractPlayerTask} should be continued or cancelled.
     * It will also cancel this {@link AbstractPlayerTask} if it became invalid.
     * 
     * @return Whether this {@link AbstractPlayerTask} is still valid
     */
    protected boolean isValid() {
        if (!p.isOnline() || !p.isValid() || p.isDead() || !p.isSneaking()) {
            cancel();
            return false;
        }

        return true;
    }

    protected abstract void executeTask();
}
