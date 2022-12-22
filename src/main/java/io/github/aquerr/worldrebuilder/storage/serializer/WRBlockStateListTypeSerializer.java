package io.github.aquerr.worldrebuilder.storage.serializer;

import io.github.aquerr.worldrebuilder.strategy.WRBlockState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WRBlockStateListTypeSerializer implements TypeSerializer<List<WRBlockState>>
{
    @Override
    public List<WRBlockState> deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        final List<WRBlockState> wrBlockStates = new ArrayList<>();
        for (final ConfigurationNode configurationNode : node.childrenList())
        {
            final WRBlockState wrBlockState = configurationNode.get(WRTypeTokens.WR_BLOCK_STATE_TYPE_TOKEN);
            wrBlockStates.add(wrBlockState);
        }
        return wrBlockStates;
    }

    @Override
    public void serialize(Type type, @Nullable List<WRBlockState> obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        for (final WRBlockState wrBlockState : obj)
        {
            final ConfigurationNode configurationNode = node.appendListNode();
            configurationNode.set(WRTypeTokens.WR_BLOCK_STATE_TYPE_TOKEN, wrBlockState);
        }
    }
}
