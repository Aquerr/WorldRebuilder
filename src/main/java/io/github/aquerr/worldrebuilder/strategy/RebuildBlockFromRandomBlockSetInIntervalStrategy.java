package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.ConstantRebuildRegionFromRandomBlockSetTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RebuildBlockFromRandomBlockSetInIntervalStrategy implements RebuildBlockFromSetStrategy
{
    private final List<BlockState> blocksToUse;

    public RebuildBlockFromRandomBlockSetInIntervalStrategy(List<BlockState> blocksToUse)
    {
        if (blocksToUse == null || blocksToUse.isEmpty())
            throw new IllegalArgumentException("Provided blocks collection must not be empty!");

        this.blocksToUse = new ArrayList<>(blocksToUse);
    }

    @Override
    public void rebuildBlocks(Region region, Collection<BlockSnapshot> blocksToRebuild)
    {
        if (isTaskAlreadyRunningForRegion(region))
            return;

        ConstantRebuildRegionFromRandomBlockSetTask constantRebuildRegionFromRandomBlockSetTask = new ConstantRebuildRegionFromRandomBlockSetTask(region.getName(), new ArrayList<>(blocksToRebuild), this.blocksToUse);
        constantRebuildRegionFromRandomBlockSetTask.setDelay(region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleTask(constantRebuildRegionFromRandomBlockSetTask);
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
        return !WorldRebuilderScheduler.getInstance().getTasksForRegion(region.getName()).isEmpty();
    }

    @Override
    public List<BlockState> getBlocksToUse()
    {
        return new ArrayList<>(blocksToUse);
    }
}
