package io.github.aquerr.worldrebuilder.strategy;

import com.google.common.base.Preconditions;
import org.spongepowered.api.block.BlockState;

public class WRBlockState
{
    private final BlockState blockState;

    public static WRBlockState of(BlockState blockState)
    {
        Preconditions.checkNotNull(blockState);
        return new WRBlockState(blockState);
    }

    private WRBlockState(BlockState blockState)
    {
        this.blockState = blockState;
    }

    public BlockState getBlockState()
    {
        return blockState;
    }
}
