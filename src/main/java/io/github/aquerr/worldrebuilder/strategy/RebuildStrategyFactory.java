package io.github.aquerr.worldrebuilder.strategy;

import java.util.List;

public final class RebuildStrategyFactory
{
    public static RebuildBlocksStrategy getStrategy(RebuildStrategyType strategyType, List<WRBlockState> predefinedBlockList)
    {
        switch (strategyType)
        {
            case RANDOM_BLOCK_FROM_LIST:
                return new RebuildBlockFromRandomBlockSetStrategy(predefinedBlockList);
            case RANDOM_BLOCK_FROM_LIST_CONTINUOUS:
                return new RebuildBlockFromRandomBlockSetInIntervalStrategy(predefinedBlockList);
            case SAME_BLOCK_CONTINUOUS:
                return new RebuildSameBlockInIntervalStrategy();
            case SAME_BLOCK:
            default:
                return new RebuildSameBlockStrategy();
        }
    }
}
