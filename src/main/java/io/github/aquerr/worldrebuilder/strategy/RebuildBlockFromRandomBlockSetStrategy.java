package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildBlocksTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class RebuildBlockFromRandomBlockSetStrategy extends AbstractRebuildBlockFromRandomBlockSetStrategy implements RebuildBlockFromSetStrategy
{
    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    public RebuildBlockFromRandomBlockSetStrategy(List<WRBlockState> blocksToUse)
    {
        super(blocksToUse);
    }

    @Override
    public void rebuildBlocks(Region region, Collection<BlockSnapshot> blocksToRebuild)
    {
        final List<BlockSnapshot> newBlocks = new ArrayList<>();
        for (final BlockSnapshot blockToRebuild : blocksToRebuild)
        {
            newBlocks.add(blockToRebuild.withState(getRandomBlock().getBlockState()));
        }

        RebuildBlocksTask rebuildBlocksTask = new RebuildBlocksTask(region.getName(), newBlocks);
        rebuildBlocksTask.setDelay(region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleTask(rebuildBlocksTask);
    }

    @Override
    public RebuildStrategyType getType()
    {
        return RebuildStrategyType.RANDOM_BLOCK_FROM_LIST;
    }

    @Override
    public boolean doesRunContinuously()
    {
        return false;
    }

    private WRBlockState getRandomBlock()
    {
        int randomIndex = THREAD_LOCAL_RANDOM.nextInt(blocksToUse.size());
        return this.blocksToUse.get(randomIndex);
    }
}
