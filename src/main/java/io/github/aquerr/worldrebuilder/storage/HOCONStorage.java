package io.github.aquerr.worldrebuilder.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import com.google.inject.Singleton;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Singleton
public class HOCONStorage implements Storage
{
	private static final String ROOT_NODE_NAME = "regions";

	private final WorldRebuilder plugin;
	private final Path regionsFilePath;

	private final ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode configNode;

	public HOCONStorage(final WorldRebuilder plugin)
	{
		this.plugin = plugin;
		final Path configDir = this.plugin.getConfigDir();

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

		this.configLoader = HoconConfigurationLoader.builder().setPath(this.regionsFilePath).setParseOptions(ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF)).build();
	}

	public void reload()
	{
		try
		{
			this.configNode = this.configLoader.load();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void addRegion(final Region region) throws ObjectMappingException
	{
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "worldUUID").setValue(TypeToken.of(UUID.class), region.getWorldUniqueId());
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "firstPoint").setValue(TypeToken.of(Vector3i.class), region.getFirstPoint());
		this.configNode.getNode(ROOT_NODE_NAME, region.getName(), "secondName").setValue(TypeToken.of(Vector3i.class), region.getSecondPoint());
	}

	public void deleteRegion(final String name)
	{
		this.configNode.getNode(ROOT_NODE_NAME).removeChild(name);
	}

	public Region getRegion(final String name) throws ObjectMappingException
	{
		final UUID worldUUID = this.configNode.getNode(ROOT_NODE_NAME, name, "worldUUID").getValue(TypeToken.of(UUID.class));
		final Vector3i firstPosition = this.configNode.getNode(ROOT_NODE_NAME, name, "firstPoint").getValue(TypeToken.of(Vector3i.class));
		final Vector3i secondPosition = this.configNode.getNode(ROOT_NODE_NAME, name, "secondName").getValue(TypeToken.of(Vector3i.class));
		return new Region(name, worldUUID, firstPosition, secondPosition);
	}

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
