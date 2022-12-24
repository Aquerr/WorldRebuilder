package io.github.aquerr.worldrebuilder.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.model.Region;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.configurate.serialize.SerializationException;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Singleton
public class StorageManager
{
	private final Storage storage;

	@Inject
	public StorageManager(final @ConfigDir(sharedRoot = false) Path configDir, ConfigManager configManager)
	{
		this.storage = new HOCONStorage(configDir, configManager);
	}

	public List<Region> getRegions()
	{
		try
		{
			return this.storage.getRegions();
		}
		catch(SerializationException e)
		{
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	public void addRegion(final Region region)
	{
		try
		{
			this.storage.addRegion(region);
		}
		catch(SerializationException e)
		{
			e.printStackTrace();
		}
	}

	public void updateRegion(final Region region)
	{
		try
		{
			this.storage.addRegion(region);
		}
		catch(SerializationException e)
		{
			e.printStackTrace();
		}
	}

	public Region getRegion(final String name)
	{
		try
		{
			return this.storage.getRegion(name);
		}
		catch(SerializationException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void deleteRegion(final String name)
	{
		this.storage.deleteRegion(name);
	}

	public void init() {
		this.storage.init();
	}
}
