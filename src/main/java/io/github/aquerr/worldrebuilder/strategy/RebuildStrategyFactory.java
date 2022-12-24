package io.github.aquerr.worldrebuilder.strategy;

import java.util.List;

public final class RebuildStrategyFactory
{
    public static RebuildBlocksStrategy getStrategy(RebuildStrategyType strategyType, List<WRBlockState> predefinedBlockList)
    {
        switch (strategyType)
        {
            case RANDOM_BLOCK_FROM_SET:
                return new RebuildBlockFromRandomBlockSetStrategy(predefinedBlockList);
            case RANDOM_BLOCK_FROM_SET_CONSTANT_IN_INTERVAL:
                return new RebuildBlockFromRandomBlockSetInIntervalStrategy(predefinedBlockList);
            case SAME_BLOCK_CONSTANT_IN_INTERVAL:
                return new RebuildSameBlockInIntervalStrategy();
            case SAME_BLOCK:
            default:
                return new RebuildSameBlockStrategy();
        }
    }
}
