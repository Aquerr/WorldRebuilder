package io.github.aquerr.worldrebuilder.scheduling;

import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;

public interface WorldRebuilderTask extends Runnable
{
    String getRegionName();

    List<Vector3i> getAffectedPositions();

    default boolean cancel()
    {
        return getTask().cancel();
    }

    void setTask(ScheduledTask task);

    ScheduledTask getTask();

    int getDelay();

    void setDelay(int delayInSeconds);
}
