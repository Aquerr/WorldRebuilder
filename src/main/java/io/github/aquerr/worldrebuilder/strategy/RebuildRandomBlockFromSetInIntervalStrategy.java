package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildRandomBlockFromSetTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderTask;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RebuildRandomBlockFromSetInIntervalStrategy implements RebuildRegionBlocksStrategy
{
    private final Set<BlockSnapshot> blocksToUse;
    private final Region region;

    public RebuildRandomBlockFromSetInIntervalStrategy(final Region region, Set<BlockSnapshot> blocksToUse)
    {
        if (region == null)
            throw new IllegalArgumentException("Provided region must not be null!");
        if (blocksToUse == null || blocksToUse.isEmpty())
            throw new IllegalArgumentException("Provided blocks collection must not be empty!");

        this.region = region;
        this.blocksToUse = blocksToUse;
        rebuildBlocks(region, Collections.emptyList());
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

    private boolean isTaskAlreadyRunningForRegion(Region region)
    {
        List<WorldRebuilderTask> tasks = WorldRebuilderScheduler.getInstance().getTasksForRegion(region.getName());
        return tasks.stream()
                .anyMatch(task -> !task.getTask().task().interval().isZero());
    }
}
