package io.github.aquerr.worldrebuilder.strategy;

public enum RebuildStrategyType
{
    SAME_BLOCK(false),
    RANDOM_BLOCK_FROM_SET(true),
    CONSTANT_REBUILD_IN_INTERVAL(false),
    CONSTANT_REBUILD_IN_INTERVAL_RANDOM_BLOCK_FROM_SET(true);

    private boolean hasPredefinedBlockSet;

    RebuildStrategyType(final boolean hasPredefinedBlockSet)
    {
        this.hasPredefinedBlockSet = hasPredefinedBlockSet;
    }

    public boolean hasPredefinedBlockSet()
    {
        return hasPredefinedBlockSet;
    }
}
