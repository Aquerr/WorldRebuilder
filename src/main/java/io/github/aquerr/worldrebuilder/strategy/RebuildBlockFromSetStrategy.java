package io.github.aquerr.worldrebuilder.strategy;

import org.spongepowered.api.block.BlockState;

import java.util.List;

public interface RebuildBlockFromSetStrategy extends RebuildBlocksStrategy
{
    List<BlockState> getBlocksToUse();
}
