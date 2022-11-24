package io.github.aquerr.worldrebuilder.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.storage.serializer.BlockSnapshotExceptionListTypeSerializer;
import io.github.aquerr.worldrebuilder.storage.serializer.BlockSnapshotExceptionTypeSerializer;
import io.github.aquerr.worldrebuilder.storage.serializer.Vector3iTypeSerializer;
import io.github.aquerr.worldrebuilder.storage.serializer.WRTypeTokens;
import io.github.aquerr.worldrebuilder.strategy.RebuildSameBlockStrategy;
import io.leangen.geantyref.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Singleton
public class HOCONStorage implements Storage
{
	private static final Logger LOGGER = LoggerFactory.getLogger(HOCONStorage.class);

	private static final String ROOT_NODE_NAME = "regions";

	private final Path regionsFilePath;

	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode configNode;

	private final ConfigManager configManager;

	@Inject
	public HOCONStorage(final Path configDir, final ConfigManager configManager)
	{
		this.configManager = configManager;
		Path storageDirPath = configDir.resolve("storage");
		this.regionsFilePath = storageDirPath.resolve("regions.conf");
	}

	@Override
	public void reload()
	{
		try
		{
			this.configNode = this.configLoader.load();
			saveChanges();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void addRegion(final Region region) throws SerializationException
	{
		this.configNode.node(ROOT_NODE_NAME, region.getName(), "worldUUID").set(TypeToken.get(UUID.class), region.getWorldUniqueId());
		this.configNode.node(ROOT_NODE_NAME, region.getName(), "firstPoint").set(TypeToken.get(Vector3i.class), region.getFirstPoint());
		this.configNode.node(ROOT_NODE_NAME, region.getName(), "secondPoint").set(TypeToken.get(Vector3i.class), region.getSecondPoint());
		this.configNode.node(ROOT_NODE_NAME, region.getName(), "restoreTime").set(region.getRestoreTime());
		this.configNode.node(ROOT_NODE_NAME, region.getName(), "active").set(region.isActive());
		this.configNode.node(ROOT_NODE_NAME, region.getName(), "shouldDropBlocks").set(region.shouldDropBlocks());

		this.configNode.node(ROOT_NODE_NAME, region.getName(), "blockSnapshotsExceptions").set(WRTypeTokens.BLOCK_EXCEPTION_LIST_TYPE_TOKEN, region.getBlockSnapshotsExceptions());
		this.configNode.node(ROOT_NODE_NAME, region.getName(), "entitySnapshotsExceptions").set(WRTypeTokens.ENTITY_SNAPSHOT_LIST_TYPE_TOKEN, region.getEntitySnapshotsExceptions());

		saveChanges();
	}

	private void saveChanges()
	{
		try
		{
			this.configLoader.save(this.configNode);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void deleteRegion(final String name)
	{
		this.configNode.node(ROOT_NODE_NAME).removeChild(name);
		saveChanges();
	}

	@Override
	public Region getRegion(final String name) throws SerializationException
	{
		try
		{
			final UUID worldUUID = this.configNode.node(ROOT_NODE_NAME, name, "worldUUID").get(TypeToken.get(UUID.class));
			final Vector3i firstPosition = this.configNode.node(ROOT_NODE_NAME, name, "firstPoint").get(TypeToken.get(Vector3i.class));
			final Vector3i secondPosition = this.configNode.node(ROOT_NODE_NAME, name, "secondPoint").get(TypeToken.get(Vector3i.class));
			final int restoreTime = this.configNode.node(ROOT_NODE_NAME, name, "restoreTime").getInt(10);
			final boolean isActive = this.configNode.node(ROOT_NODE_NAME, name, "active").getBoolean(true);
			final boolean shouldDropBlocks = this.configNode.node(ROOT_NODE_NAME, name, "shouldDropBlocks").getBoolean(true);

			final List<BlockSnapshot> blockSnapshotsExceptions = this.configNode.node(ROOT_NODE_NAME, name, "blockSnapshotsExceptions").get(WRTypeTokens.BLOCK_EXCEPTION_LIST_TYPE_TOKEN, Collections.emptyList());
			final List<EntitySnapshot> entitySnapshotsExceptions = this.configNode.node(ROOT_NODE_NAME, name, "entitySnapshotsExceptions").getList(TypeToken.get(EntitySnapshot.class), Collections.emptyList());
			return new Region(name, worldUUID, firstPosition, secondPosition, restoreTime, isActive, shouldDropBlocks, new LinkedList<>(blockSnapshotsExceptions), new LinkedList<>(entitySnapshotsExceptions), new RebuildSameBlockStrategy());
		}
		catch (Exception exception)
		{
			LOGGER.error("Could not load region {} from storage. Reason: {}", name, exception.getMessage());
			throw exception;
		}
	}

	@Override
	public List<Region> getRegions() throws SerializationException
	{
		final List<Region> regions = new LinkedList<>();
		final ConfigurationNode regionsNode = this.configNode.node(ROOT_NODE_NAME);
		final Map<Object, ? extends ConfigurationNode> regionNodes = regionsNode.childrenMap();
		final Set<Object> regionsNames = regionNodes.keySet();
		for (final Object regionName : regionsNames)
		{
			if(!(regionName instanceof String))
				continue;
			final Region arena = getRegion(String.valueOf(regionName));
			regions.add(arena);
		}
		return regions;
	}

	@Override
	public void init()
	{
		//Prepare storage directory
		try
		{
			Files.createDirectories(this.regionsFilePath.getParent());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if(Files.notExists(this.regionsFilePath))
		{
			try
			{
				Files.createFile(this.regionsFilePath);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}

		this.configLoader = HoconConfigurationLoader.builder()
				.path(this.regionsFilePath)
				.defaultOptions(getDefaultOptions())
				.build();
		reload();
	}

	private ConfigurationOptions getDefaultOptions()
	{
		TypeSerializerCollection collection = TypeSerializerCollection.builder()
				.registerAll(TypeSerializerCollection.defaults())
				.register(WRTypeTokens.BLOCK_EXCEPTION_TYPE_TOKEN, new BlockSnapshotExceptionTypeSerializer())
				.register(WRTypeTokens.BLOCK_EXCEPTION_LIST_TYPE_TOKEN, new BlockSnapshotExceptionListTypeSerializer())
				.register(WRTypeTokens.VECTOR3I_TYPE_TOKEN, new Vector3iTypeSerializer())
				.registerAll(configManager.serializers())
				.build();

		final ConfigurationOptions configurationOptions = ConfigurationOptions.defaults()
				.serializers(collection);

		return configurationOptions;
	}
}
