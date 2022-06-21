package io.github.aquerr.worldrebuilder.storage.serializer;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jline.utils.InputStreamReader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.lang.String.format;

public class BlockExceptionTypeSerializer implements TypeSerializer<BlockSnapshot> {

    @Override
    public @Nullable BlockSnapshot deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        DataContainer dataContainer = null;
        try
        {
            StringWriter writer = new StringWriter();
            HoconConfigurationLoader.builder()
                    .setSink(() -> new BufferedWriter(writer))
                    .build()
                    .save(value);
            String itemNodeAsString = writer.toString();
            dataContainer = DataFormats.HOCON.read(itemNodeAsString);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        final String blockTypeId = dataContainer.get(DataQuery.of("BlockType"))
                .map(String::valueOf)
                .orElse("");

        if ("".equals(blockTypeId))
        {
            throw new ObjectMappingException(format("BlockType id is empty. Data Container: %s", dataContainer));
        }
        final Optional<BlockType> blockType = Sponge.getRegistry().getType(BlockType.class, blockTypeId);
        if (!blockType.isPresent())
        {
            throw new ObjectMappingException(format("BlockType %s could not be recognized. Probably comes from a mod that has been removed from the server.", blockTypeId));
        }

        try
        {
            Optional<BlockSnapshot> blockSnapshot = BlockSnapshot.builder().build(dataContainer);
            return blockSnapshot.orElse(null);
        }
        catch (Exception exception)
        {
            throw new ObjectMappingException(format("Could not crate BlockSnapshot from data container %s", dataContainer));
        }
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable BlockSnapshot obj, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        if (obj == null)
            return;

        DataContainer dataContainer = obj.toContainer();
        try
        {
            String blockSnapshotAsString = DataFormats.HOCON.write(dataContainer);
            ConfigurationNode node = HoconConfigurationLoader.builder()
                    .setSource(() -> new BufferedReader(new InputStreamReader(new ByteArrayInputStream(blockSnapshotAsString.getBytes(StandardCharsets.UTF_8)))))
                    .build()
                    .load();

            node.setValue(node);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
