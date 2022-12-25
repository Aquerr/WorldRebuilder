package io.github.aquerr.worldrebuilder.strategy;

public enum RebuildStrategyType
{
    SAME_BLOCK(false, false),
    RANDOM_BLOCK_FROM_LIST(true, false),
    SAME_BLOCK_CONTINUOUS(false, true),
    RANDOM_BLOCK_FROM_LIST_CONTINUOUS(true, true);

    private final boolean hasPredefinedBlockSet;
    private final boolean doesRunContinuously;

    RebuildStrategyType(final boolean hasPredefinedBlockSet, final boolean doesRunContinuously)
    {
        this.hasPredefinedBlockSet = hasPredefinedBlockSet;
        this.doesRunContinuously = doesRunContinuously;
    }

    public boolean hasPredefinedBlockSet()
    {
        return hasPredefinedBlockSet;
    }

    public boolean isDoesRunContinuously()
    {
        return this.doesRunContinuously;
    }
}
