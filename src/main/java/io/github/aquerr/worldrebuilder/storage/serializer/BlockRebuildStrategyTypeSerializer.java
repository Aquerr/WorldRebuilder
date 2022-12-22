package io.github.aquerr.worldrebuilder.storage.serializer;

import io.github.aquerr.worldrebuilder.strategy.RebuildBlockFromSetStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildBlockFromRandomBlockSetInIntervalStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildBlockFromRandomBlockSetStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildBlocksStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildInIntervalStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildSameBlockStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyType;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockRebuildStrategyTypeSerializer implements TypeSerializer<RebuildBlocksStrategy>
{
    private static final String NODE_TYPE = "type";
    private static final String NODE_BLOCKS_TO_USE = "blocksToUse";

    @Override
    public RebuildBlocksStrategy deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        RebuildStrategyType strategyType = node.node(NODE_TYPE).get(TypeToken.get(RebuildStrategyType.class), RebuildStrategyType.SAME_BLOCK);
        boolean doesRunContinuously = strategyType.isDoesRunContinuously();
        if (strategyType.hasPredefinedBlockSet())
        {
            List<BlockState> blocks = node.node(NODE_BLOCKS_TO_USE).get(new TypeToken<List<BlockState>>() {}, Collections.emptyList());
            if (doesRunContinuously)
            {
                return new RebuildBlockFromRandomBlockSetInIntervalStrategy(new ArrayList<>(blocks));
            }
            return new RebuildBlockFromRandomBlockSetStrategy(new ArrayList<>(blocks));
        }

        if (doesRunContinuously)
        {
            return new RebuildInIntervalStrategy();
        }
        return new RebuildSameBlockStrategy();
    }

    @Override
    public void serialize(Type type, @Nullable RebuildBlocksStrategy obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node(NODE_TYPE).set(obj.getType().name());

        if (obj instanceof RebuildBlockFromSetStrategy)
        {
            RebuildBlockFromSetStrategy rebuildBlockFromSetStrategy = (RebuildBlockFromSetStrategy)obj;
            node.node(NODE_BLOCKS_TO_USE).setList(new TypeToken<BlockState>() {}, new ArrayList<>(rebuildBlockFromSetStrategy.getBlocksToUse()));
        }
    }
}
