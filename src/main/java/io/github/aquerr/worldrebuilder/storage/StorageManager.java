package io.github.aquerr.worldrebuilder.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.Collections;
import java.util.List;

@Singleton
public class StorageManager
{
	private final WorldRebuilder plugin;
	private final Storage storage;

	@Inject
	public StorageManager(final WorldRebuilder plugin)
	{
		this.plugin = plugin;
		this.storage = new HOCONStorage(plugin);
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
}
