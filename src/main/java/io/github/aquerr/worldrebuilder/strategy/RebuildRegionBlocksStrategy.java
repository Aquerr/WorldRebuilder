package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.Collection;

@FunctionalInterface
public interface RebuildRegionBlocksStrategy
{
    void rebuildBlocks(Region region, Collection<BlockSnapshot> blocks);
}
