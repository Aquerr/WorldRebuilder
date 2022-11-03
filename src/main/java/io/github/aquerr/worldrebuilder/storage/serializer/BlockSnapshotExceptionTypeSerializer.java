package io.github.aquerr.worldrebuilder.storage.serializer;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jline.utils.InputStreamReader;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static java.lang.String.format;

public class BlockSnapshotExceptionTypeSerializer implements TypeSerializer<BlockSnapshot>
{
    @Override
    public BlockSnapshot deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        DataContainer dataContainer = null;
        try
        {
            StringWriter writer = new StringWriter();
            HoconConfigurationLoader.builder()
                    .sink(() -> new BufferedWriter(writer))
                    .build()
                    .save(node);
            String itemNodeAsString = writer.toString();
            dataContainer = DataFormats.HOCON.get().read(itemNodeAsString);
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
            throw new SerializationException(format("BlockType id is empty. Data Container: %s", dataContainer));
        }
//        final Optional<BlockType> blockType = Sponge.game().findRegistry(RegistryType.of()).get();
//        if (!blockType.isPresent())
//        {
//            throw new ObjectMappingException(format("BlockType %s could not be recognized. Probably comes from a mod that has been removed from the server.", blockTypeId));
//        }

        try
        {
            Optional<BlockSnapshot> blockSnapshot = BlockSnapshot.builder().build(dataContainer);
            return blockSnapshot.orElse(null);
        }
        catch (Exception exception)
        {
            throw new SerializationException(format("Could not crate BlockSnapshot from data container %s", dataContainer));
        }
    }

    @Override
    public void serialize(Type type, @Nullable BlockSnapshot obj, ConfigurationNode value) throws SerializationException
    {
        if (obj == null)
            return;

        DataContainer dataContainer = obj.toContainer();
        try
        {
            String blockSnapshotAsString = DataFormats.HOCON.get().write(dataContainer);
            ConfigurationNode node = HoconConfigurationLoader.builder()
                    .source(() -> new BufferedReader(new InputStreamReader(new ByteArrayInputStream(blockSnapshotAsString.getBytes(StandardCharsets.UTF_8)))))
                    .build()
                    .load();

            node.set(node);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
