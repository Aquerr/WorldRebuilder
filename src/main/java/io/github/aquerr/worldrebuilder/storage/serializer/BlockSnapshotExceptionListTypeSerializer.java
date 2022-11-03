package io.github.aquerr.worldrebuilder.storage.serializer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BlockSnapshotExceptionListTypeSerializer implements TypeSerializer<List<BlockSnapshot>>
{
    @Override
    public List<BlockSnapshot> deserialize(Type type, ConfigurationNode value) throws SerializationException
    {
        final List<BlockSnapshot> blockSnapshots = new ArrayList<>();
        for (final ConfigurationNode configurationNode : value.childrenList())
        {
            final BlockSnapshot blockSnapshot = configurationNode.get(WRTypeTokens.BLOCK_EXCEPTION_TYPE_TOKEN);
            blockSnapshots.add(blockSnapshot);
        }
        return blockSnapshots;    }

    @Override
    public void serialize(Type type, @Nullable List<BlockSnapshot> blockSnapshots, ConfigurationNode value) throws SerializationException
    {
        if (blockSnapshots == null)
            return;

        for (final BlockSnapshot blockSnapshot : blockSnapshots)
        {
            final ConfigurationNode configurationNode = value.appendListNode();
            configurationNode.set(WRTypeTokens.BLOCK_EXCEPTION_TYPE_TOKEN, blockSnapshot);
        }
    }
}
