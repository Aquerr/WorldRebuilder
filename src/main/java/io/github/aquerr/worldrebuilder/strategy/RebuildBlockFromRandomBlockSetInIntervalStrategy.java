package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.scheduling.ConstantRebuildRegionFromRandomBlockSetTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class RebuildBlockFromRandomBlockSetInIntervalStrategy extends AbstractRebuildBlockFromRandomBlockSetStrategy implements RebuildBlockFromSetStrategy
{
    public RebuildBlockFromRandomBlockSetInIntervalStrategy(List<WRBlockState> blocksToUse)
    {
        super(blocksToUse);
    }

    @Override
    public void rebuildBlocks(Region region, Collection<BlockSnapshot> blocksToRebuild)
    {
        if (isTaskAlreadyRunningForRegion(region))
            return;

        ConstantRebuildRegionFromRandomBlockSetTask constantRebuildRegionFromRandomBlockSetTask = new ConstantRebuildRegionFromRandomBlockSetTask(region, new ArrayList<>(blocksToRebuild), this.blocksToUse, region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleIntervalTask(constantRebuildRegionFromRandomBlockSetTask);
    }

    @Override
    public RebuildStrategyType getType()
    {
        return RebuildStrategyType.RANDOM_BLOCK_FROM_SET_CONSTANT_IN_INTERVAL;
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
}
