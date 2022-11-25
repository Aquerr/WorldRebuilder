package io.github.aquerr.worldrebuilder.storage.serializer;

import io.github.aquerr.worldrebuilder.strategy.RebuildBlockFromSetStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildRandomBlockFromSetInIntervalStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildRandomBlockFromSetStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildRegionBlocksStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildRegionInIntervalStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildSameBlockStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyType;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class BlockRebuildStrategyTypeSerializer implements TypeSerializer<RebuildRegionBlocksStrategy>
{
    @Override
    public RebuildRegionBlocksStrategy deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        RebuildStrategyType strategyType = node.node("type").get(TypeToken.get(RebuildStrategyType.class), RebuildStrategyType.SAME_BLOCK);
        boolean doesRunContinuously = node.node("doesRunContinuously").getBoolean(false);
        if (strategyType.hasPredefinedBlockSet())
        {
            Collection<BlockSnapshot> blocks = node.node("blocksToUse").get(WRTypeTokens.BLOCK_SNAPSHOT_COLLECTION_TYPE_TOKEN, Collections.emptyList());
            if (doesRunContinuously)
            {
                return new RebuildRandomBlockFromSetInIntervalStrategy(new HashSet<>(blocks));
            }
            return new RebuildRandomBlockFromSetStrategy(new HashSet<>(blocks));
        }

        if (doesRunContinuously)
        {
            return new RebuildRegionInIntervalStrategy();
        }
        return new RebuildSameBlockStrategy();
    }

    @Override
    public void serialize(Type type, @Nullable RebuildRegionBlocksStrategy obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("type").set(obj.getType().name());
        node.node("doesRunContinuously").set(obj.doesRunContinuously());

        if (obj instanceof RebuildBlockFromSetStrategy)
        {
            RebuildBlockFromSetStrategy rebuildBlockFromSetStrategy = (RebuildBlockFromSetStrategy)obj;
            node.node("blocksToUse").set(WRTypeTokens.BLOCK_SNAPSHOT_COLLECTION_TYPE_TOKEN, rebuildBlockFromSetStrategy.getBlocksToUse());
        }
    }
}
