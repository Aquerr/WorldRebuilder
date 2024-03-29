package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.scheduling.ConstantRebuildRegionBlocksTask;
import io.github.aquerr.worldrebuilder.scheduling.RebuildBlocksTask;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class RebuildSameBlockInIntervalStrategy implements RebuildBlocksStrategy
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

        RebuildBlocksTask rebuildBlocksTask = new ConstantRebuildRegionBlocksTask(region, blocksToRebuild, region.getRestoreTime());
        WorldRebuilderScheduler.getInstance().scheduleIntervalTaskAsync(rebuildBlocksTask);
    }

    @Override
    public RebuildStrategyType getType()
    {
        return RebuildStrategyType.SAME_BLOCK_CONTINUOUS;
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

    private Collection<? extends BlockSnapshot> getBlocksFromRegion(Region region)
    {
        return WorldUtils.getWorldByUUID(region.getWorldUniqueId())
                .map(serverWorld -> getBlocksFromRegion(region, serverWorld))
                .orElse(Collections.emptyList());
    }

    private List<BlockSnapshot> getBlocksFromRegion(Region region, ServerWorld serverWorld)
    {
        AABB aabb = AABB.of(region.getFirstPoint(), region.getSecondPoint());
        return serverWorld.blockStateStream(aabb.min().toInt(), aabb.max().toInt(), StreamOptions.forceLoadedAndCopied())
                .map(element -> element.type().snapshotFor(ServerLocation.of(element.volume(), element.position())))
                .toStream()
                .map(VolumeElement::type)
                .collect(Collectors.toList());
    }
}
