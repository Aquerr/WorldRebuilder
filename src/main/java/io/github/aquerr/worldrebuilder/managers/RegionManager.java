package io.github.aquerr.worldrebuilder.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderTask;
import io.github.aquerr.worldrebuilder.storage.StorageManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class RegionManager
{
	private final StorageManager storageManager;
	private final Map<String, Region> regions = new ConcurrentHashMap<>();

	@Inject
	public RegionManager(final StorageManager storageManager)
	{
		this.storageManager = storageManager;
	}

	public Collection<Region> getRegions()
	{
		return this.regions.values();
	}

	public void loadRegions()
	{
		final List<Region> regions = this.storageManager.getRegions();
		for(final Region region : regions)
		{
			this.regions.put(region.getName(), region);
		}
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

	public Region getRegion(final String name)
	{
		return this.regions.get(name);
	}

	public void forceRebuildRegion(final Region region)
	{
		WorldRebuilderScheduler worldRebuilderScheduler = WorldRebuilderScheduler.getInstance();
		List<WorldRebuilderTask> worldRebuilderTasks = new LinkedList<>(worldRebuilderScheduler.getTasksForRegion(region.getName()));
		worldRebuilderScheduler.cancelTasksForRegion(region.getName());
		for (final WorldRebuilderTask worldRebuilderTask : worldRebuilderTasks)
		{
			worldRebuilderTask.run();
		}
	}

	public void init() {
		this.storageManager.init();
		loadRegions();
	}
}
