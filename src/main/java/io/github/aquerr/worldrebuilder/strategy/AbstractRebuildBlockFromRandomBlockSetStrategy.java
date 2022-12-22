package io.github.aquerr.worldrebuilder.strategy;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractRebuildBlockFromRandomBlockSetStrategy implements RebuildBlockFromSetStrategy
{
    protected final List<WRBlockState> blocksToUse;

    protected AbstractRebuildBlockFromRandomBlockSetStrategy(List<WRBlockState> blocksToUse)
    {
        if (blocksToUse == null || blocksToUse.isEmpty())
            throw new IllegalArgumentException("Provided blocks collection must not be empty!");

        this.blocksToUse = new ArrayList<>(blocksToUse);
    }

    @Override
    public List<WRBlockState> getBlocksToUse()
    {
        return new ArrayList<>(blocksToUse);
    }
}
