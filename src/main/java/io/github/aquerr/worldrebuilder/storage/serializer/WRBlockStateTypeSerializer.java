package io.github.aquerr.worldrebuilder.storage.serializer;

import io.github.aquerr.worldrebuilder.strategy.WRBlockState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class WRBlockStateTypeSerializer implements TypeSerializer<WRBlockState>
{

    @Override
    public WRBlockState deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        BlockState blockState = node.node("blockState").get(BlockState.class);
        return WRBlockState.of(blockState);
    }

    @Override
    public void serialize(Type type, @Nullable WRBlockState obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("blockState").set(obj.getBlockState());
    }
}
