package io.github.aquerr.worldrebuilder.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.entity.Region;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.util.TypeTokens;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class HOCONStorage implements Storage
{
	private static final TypeToken<List<BlockSnapshot>> BLOCKSNAPSHOT_TYPE_TOKEN = new TypeToken<List<BlockSnapshot>>() {};
	private static final TypeToken<List<EntitySnapshot>> ENTITY_SNAPSHOT_LIST_TYPE_TOKEN = new TypeToken<List<EntitySnapshot>>() {};

	private static final String ROOT_NODE_NAME = "regions";

	private final Path regionsFilePath;

	private final ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode configNode;

	@Inject
	public HOCONStorage(final Path configDir)
	{
//		final Path configDir = this.plugin.getConfigDir();

		//Prepare storage directory
		Path storageDirPath = configDir.resolve("storage");
		if(Files.notExists(storageDirPath))
		{
			try
			{
				Files.createDirectories(storageDirPath);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		this.regionsFilePath = storageDirPath.resolve("regions.conf");
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

		this.configLoader = HoconConfigurationLoader.builder().setPath(this.regionsFilePath).build();
		reload();
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
	public void addRegion(final Region region) throws ObjectMappingException
	{
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "worldUUID").setValue(TypeToken.of(UUID.class), region.getWorldUniqueId());
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "firstPoint").setValue(TypeToken.of(Vector3i.class), region.getFirstPoint());
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "secondName").setValue(TypeToken.of(Vector3i.class), region.getSecondPoint());
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "restoreTime").setValue(region.getRestoreTime());
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "active").setValue(region.isActive());
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "shouldDropBlocks").setValue(region.shouldDropBlocks());

		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "blockSnapshotsExceptions").setValue(BLOCKSNAPSHOT_TYPE_TOKEN, region.getBlockSnapshotsExceptions());
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "entitySnapshotsExceptions").setValue(ENTITY_SNAPSHOT_LIST_TYPE_TOKEN, region.getEntitySnapshotsExceptions());

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
		this.configNode.getNode(ROOT_NODE_NAME).removeChild(name);
		saveChanges();
	}

	@Override
	public Region getRegion(final String name) throws ObjectMappingException
	{
		final UUID worldUUID = this.configNode.getNode(ROOT_NODE_NAME, name, "worldUUID").getValue(TypeToken.of(UUID.class));
		final Vector3i firstPosition = this.configNode.getNode(ROOT_NODE_NAME, name, "firstPoint").getValue(TypeToken.of(Vector3i.class));
		final Vector3i secondPosition = this.configNode.getNode(ROOT_NODE_NAME, name, "secondName").getValue(TypeToken.of(Vector3i.class));
		final int restoreTime = this.configNode.getNode(ROOT_NODE_NAME, name, "restoreTime").getInt(10);
		final boolean isActive = this.configNode.getNode(ROOT_NODE_NAME, name, "active").getBoolean(true);
		final boolean shouldDropBlocks = this.configNode.getNode(ROOT_NODE_NAME, name, "shouldDropBlocks").getBoolean(true);

		//TODO: Create custom TypeSerializer
		final List<BlockSnapshot> blockSnapshotsExceptions = this.configNode.getNode(ROOT_NODE_NAME, name, "blockSnapshotsExceptions").getValue(BLOCKSNAPSHOT_TYPE_TOKEN);
		final List<EntitySnapshot> entitySnapshotsExceptions = this.configNode.getNode(ROOT_NODE_NAME, name, "entitySnapshotsExceptions").getList(TypeTokens.ENTITY_TOKEN);

		return new Region(name, worldUUID, firstPosition, secondPosition, restoreTime, isActive, shouldDropBlocks, blockSnapshotsExceptions, entitySnapshotsExceptions);
	}

	@Override
	public List<Region> getRegions() throws ObjectMappingException
	{
		final List<Region> regions = new LinkedList<>();
		final ConfigurationNode regionsNode = this.configNode.getNode(ROOT_NODE_NAME);
		final Map<Object, ? extends ConfigurationNode> regionNodes = regionsNode.getChildrenMap();
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
}
