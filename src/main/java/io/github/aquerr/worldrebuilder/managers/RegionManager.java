package io.github.aquerr.worldrebuilder.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.storage.StorageManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RegionManager
{
	private final StorageManager storageManager;
	private final Map<String, Region> regions = new HashMap<>();

	@Inject
	public RegionManager(final StorageManager storageManager)
	{
		this.storageManager = storageManager;
		loadRegions();
	}

	public Collection<Region> getRegions()
	{
		return this.regions.values();
	}

	public void loadRegions()
	{
		CompletableFuture.runAsync(() -> {
			final List<Region> regions = this.storageManager.getRegions();
			for(final Region region : regions)
			{
				this.regions.put(region.getName(), region);
			}
		});
	}

	public void addRegion(final Region region)
	{
		CompletableFuture.runAsync(() -> this.storageManager.addRegion(region));
		this.regions.put(region.getName(), region);
	}

	public void updateRegion(final Region region)
	{
		CompletableFuture.runAsync(() -> this.storageManager.updateRegion(region));
		this.regions.put(region.getName(), region);
	}

	public void deleteRegion(final String name)
	{
		CompletableFuture.runAsync(() -> this.storageManager.deleteRegion(name));
		this.regions.remove(name);
	}

	public @Nullable Region getRegion(final String name)
	{
		return this.regions.get(name);
	}
}
