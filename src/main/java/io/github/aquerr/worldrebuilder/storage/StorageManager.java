package io.github.aquerr.worldrebuilder.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.config.ConfigDir;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

@Singleton
public class StorageManager
{
	private final Storage storage;

	@Inject
	public StorageManager(final @ConfigDir(sharedRoot = false) Path configDir)
	{
		this.storage = new HOCONStorage(configDir);
	}

	public List<Region> getRegions()
	{
		try
		{
			return this.storage.getRegions();
		}
		catch(ObjectMappingException e)
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
		catch(ObjectMappingException e)
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
		catch(ObjectMappingException e)
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
		catch(ObjectMappingException e)
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
