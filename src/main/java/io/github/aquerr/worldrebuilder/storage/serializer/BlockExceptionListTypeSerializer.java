package io.github.aquerr.worldrebuilder.storage.serializer;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BlockExceptionListTypeSerializer implements TypeSerializer<List<BlockSnapshot>> {

    @Override
    public @Nullable List<BlockSnapshot> deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        final List<BlockSnapshot> blockSnapshots = new ArrayList<>();
        for (final ConfigurationNode configurationNode : value.getChildrenList())
        {
            final BlockSnapshot blockSnapshot = configurationNode.getValue(WRTypeTokens.BLOCK_EXCEPTION);
            blockSnapshots.add(blockSnapshot);
        }
        return blockSnapshots;
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable List<BlockSnapshot> blockSnapshots, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        if (blockSnapshots == null)
            return;

        for (final BlockSnapshot blockSnapshot : blockSnapshots)
        {
            final ConfigurationNode configurationNode = value.appendListNode();
            configurationNode.setValue(WRTypeTokens.BLOCK_EXCEPTION, blockSnapshot);
        }
    }
}
