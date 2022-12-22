package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildBlocksTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RebuildBlockFromRandomBlockSetStrategy implements RebuildBlockFromSetStrategy
{
    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    private final List<BlockState> blocksToUse;

    public RebuildBlockFromRandomBlockSetStrategy(List<BlockState> blocksToUse)
    {
        if (blocksToUse == null || blocksToUse.isEmpty())
            throw new IllegalArgumentException("Provided blocks collection must not be empty!");

        this.blocksToUse = new ArrayList<>(blocksToUse);
    }

    @Override
    public void rebuildBlocks(Region region, Collection<BlockSnapshot> blocksToRebuild)
    {
        final List<BlockSnapshot> newBlocks = new ArrayList<>();
        for (final BlockSnapshot blockToRebuild : blocksToRebuild)
        {
            newBlocks.add(blockToRebuild.withState(getRandomBlock()));
        }

        RebuildBlocksTask rebuildBlocksTask = new RebuildBlocksTask(region.getName(), newBlocks);
        rebuildBlocksTask.setDelay(region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleTask(rebuildBlocksTask);
    }

    @Override
    public RebuildStrategyType getType()
    {
        return RebuildStrategyType.RANDOM_BLOCK_FROM_SET;
    }

    @Override
    public boolean doesRunContinuously()
    {
        return false;
    }

    private BlockState getRandomBlock()
    {
        int randomIndex = THREAD_LOCAL_RANDOM.nextInt(blocksToUse.size());
        return this.blocksToUse.get(randomIndex);
    }

    @Override
    public List<BlockState> getBlocksToUse()
    {
        return new ArrayList<>(blocksToUse);
    }
}
