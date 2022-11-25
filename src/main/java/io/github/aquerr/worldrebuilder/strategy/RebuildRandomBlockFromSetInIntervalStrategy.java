package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildRandomBlockFromSetTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderTask;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RebuildRandomBlockFromSetInIntervalStrategy implements RebuildBlockFromSetStrategy
{
    private final Set<BlockSnapshot> blocksToUse;

    public RebuildRandomBlockFromSetInIntervalStrategy(Set<BlockSnapshot> blocksToUse)
    {
        if (blocksToUse == null || blocksToUse.isEmpty())
            throw new IllegalArgumentException("Provided blocks collection must not be empty!");

        this.blocksToUse = blocksToUse;
    }

    @Override
    public void rebuildBlocks(Region region, Collection<BlockSnapshot> blocksToRebuild)
    {
        if (isTaskAlreadyRunningForRegion(region))
            return;

        RebuildRandomBlockFromSetTask rebuildRandomBlockFromSetTask = new RebuildRandomBlockFromSetTask(region.getName(), region.getWorldUniqueId(), new ArrayList<>(blocksToRebuild), this.blocksToUse);
        rebuildRandomBlockFromSetTask.setDelay(region.getRestoreTime());
        rebuildRandomBlockFromSetTask.setInterval(region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleTask(rebuildRandomBlockFromSetTask);
    }

    @Override
    public RebuildStrategyType getType()
    {
        return RebuildStrategyType.CONSTANT_REBUILD_IN_INTERVAL_RANDOM_BLOCK_FROM_SET;
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

    @Override
    public Collection<BlockSnapshot> getBlocksToUse()
    {
        return new ArrayList<>(blocksToUse);
    }
}
