package io.github.aquerr.worldrebuilder.scheduling;

import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.UUID;

public interface WorldRebuilderTask extends Runnable
{
    String getRegionName();

    List<Vector3i> getAffectedPositions();

    UUID getWorldUniqueId();

    boolean cancel();
}