package io.github.aquerr.worldrebuilder.strategy;

import org.spongepowered.api.block.BlockSnapshot;

import java.util.Collection;

public interface RebuildBlockFromSetStrategy extends RebuildRegionBlocksStrategy
{
    Collection<BlockSnapshot> getBlocksToUse();
}
