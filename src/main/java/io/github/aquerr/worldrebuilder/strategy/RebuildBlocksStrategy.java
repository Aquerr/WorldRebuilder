package io.github.aquerr.worldrebuilder.strategy;

import io.github.aquerr.worldrebuilder.model.Region;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.Collection;

public interface RebuildBlocksStrategy
{
    void rebuildBlocks(Region region, Collection<BlockSnapshot> blocks);

    RebuildStrategyType getType();

    boolean doesRunContinuously();
}
