package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildBlocksTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.ArrayList;
import java.util.Collection;

public class RebuildSameBlockStrategy implements RebuildBlocksStrategy
{
    @Override
    public void rebuildBlocks(Region region, Collection<BlockSnapshot> blockSnapshots)
    {
        RebuildBlocksTask rebuildBlocksTask = new RebuildBlocksTask(region.getName(), new ArrayList<>(blockSnapshots));
        rebuildBlocksTask.setDelay(region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleTask(rebuildBlocksTask);
    }

    @Override
    public RebuildStrategyType getType()
    {
        return RebuildStrategyType.SAME_BLOCK;
    }

    @Override
    public boolean doesRunContinuously()
    {
        return false;
    }
}
