package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildBlocksTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RebuildRandomBlockFromSetStrategy implements RebuildRegionBlocksStrategy
{
    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    private final List<BlockSnapshot> blockSnapshots;

    public RebuildRandomBlockFromSetStrategy(Set<BlockSnapshot> blockSnapshots)
    {
        if (blockSnapshots == null || blockSnapshots.isEmpty())
            throw new IllegalArgumentException("Provided blocks collection must not be empty!");

        this.blockSnapshots = new ArrayList<>(blockSnapshots);
    }

    @Override
    public void rebuildBlocks(Region region, Collection<BlockSnapshot> blocksToRebuild)
    {
        final List<BlockSnapshot> newBlocks = new ArrayList<>();
        for (final BlockSnapshot blockToRebuild : blocksToRebuild)
        {
            blockToRebuild.location()
                    .map(blockLocation -> getRandomBlock().withLocation(blockLocation))
                    .ifPresent(newBlocks::add);
        }

        RebuildBlocksTask rebuildBlocksTask = new RebuildBlocksTask(region.getName(), region.getWorldUniqueId(), newBlocks);
        rebuildBlocksTask.setDelay(region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleTask(rebuildBlocksTask);
    }

    private BlockSnapshot getRandomBlock()
    {
        int randomIndex = THREAD_LOCAL_RANDOM.nextInt(blockSnapshots.size());
        return this.blockSnapshots.get(randomIndex);
    }
}
