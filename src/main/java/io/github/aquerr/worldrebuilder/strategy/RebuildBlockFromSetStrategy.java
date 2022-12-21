package io.github.aquerr.worldrebuilder.strategy;

import org.spongepowered.api.block.BlockState;

import java.util.Set;

public interface RebuildBlockFromSetStrategy extends RebuildBlocksStrategy
{
    Set<BlockState> getBlocksToUse();
}
