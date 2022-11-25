package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildBlocksTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderTask;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RebuildRegionInIntervalStrategy implements RebuildRegionBlocksStrategy
{
    @Override
    public void rebuildBlocks(Region region, Collection<BlockSnapshot> blocks)
    {
        if (isTaskAlreadyRunningForRegion(region))
            return;

        List<BlockSnapshot> blocksToRebuild = new ArrayList<>(blocks);
        blocksToRebuild.addAll(getBlocksFromRegion(region));

        if (blocksToRebuild.isEmpty())
            return;

        RebuildBlocksTask rebuildBlocksTask = new RebuildBlocksTask(region.getName(), region.getWorldUniqueId(), blocksToRebuild);
        rebuildBlocksTask.setInterval(region.getRestoreTime());
        rebuildBlocksTask.setDelay(region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleTask(rebuildBlocksTask);
    }

    @Override
    public RebuildStrategyType getType()
    {
        return RebuildStrategyType.CONSTANT_REBUILD_IN_INTERVAL;
    }

    @Override
    public boolean doesRunContinuously()
    {
        return true;
    }

    private boolean isTaskAlreadyRunningForRegion(Region region)
    {
        List<WorldRebuilderTask> tasks = WorldRebuilderScheduler.getInstance().getTasksForRegion(region.getName());
        return tasks.stream()
                .anyMatch(task -> !task.getTask().task().interval().isZero());
    }

    private Collection<? extends BlockSnapshot> getBlocksFromRegion(Region region)
    {
        return WorldUtils.getWorldByUUID(region.getWorldUniqueId())
                .map(serverWorld -> getBlocksFromRegion(region, serverWorld))
                .orElse(Collections.emptyList());
    }

    private List<BlockSnapshot> getBlocksFromRegion(Region region, ServerWorld serverWorld)
    {
        return serverWorld.blockStateStream(region.getFirstPoint(), region.getSecondPoint(), StreamOptions.lazily())
                .map(element -> element.type().snapshotFor(ServerLocation.of(element.volume(), element.position())))
                .toStream()
                .map(VolumeElement::type)
                .collect(Collectors.toList());
    }
}