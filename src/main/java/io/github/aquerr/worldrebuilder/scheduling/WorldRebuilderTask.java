package io.github.aquerr.worldrebuilder.scheduling;

import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.UUID;

public interface WorldRebuilderTask extends Runnable
{
    String getRegionName();

    List<Vector3i> getAffectedPositions();

    UUID getWorldUniqueId();

    default boolean cancel()
    {
        return getTask().cancel();
    }

    void setTask(ScheduledTask task);

    ScheduledTask getTask();

    int getInterval();

    void setInterval(int intervalInSeconds);

    int getDelay();

    void setDelay(int delayInSeconds);
}
