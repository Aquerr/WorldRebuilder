package io.github.aquerr.worldrebuilder.strategy;

import java.util.List;

public interface RebuildBlockFromSetStrategy extends RebuildBlocksStrategy
{
    List<WRBlockState> getBlocksToUse();
}
