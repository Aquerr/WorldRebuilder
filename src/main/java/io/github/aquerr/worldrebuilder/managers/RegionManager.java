package io.github.aquerr.worldrebuilder.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderTask;
import io.github.aquerr.worldrebuilder.storage.StorageManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class RegionManager
{
	private final StorageManager storageManager;
	private final Logger logger;
	private final Map<String, Region> regions = new ConcurrentHashMap<>();

	@Inject
	public RegionManager(final StorageManager storageManager, final Logger logger)
	{
		this.storageManager = storageManager;
		this.logger = logger;
	}

	public Collection<Region> getRegions()
	{
		return this.regions.values();
	}

	public void reloadRegions()
	{
		final List<Region> regions = this.storageManager.getRegions();
		for(final Region region : regions)
		{
			this.regions.put(region.getName(), region);
		}

		startRebuildForContinuousRegions();
	}

	public void addRegion(final Region region)
	{
		WorldRebuilderScheduler.getInstance().queueStorageTask(() -> this.storageManager.addRegion(region));
		this.regions.put(region.getName(), region);
	}

	public void updateRegion(final Region region)
	{
		WorldRebuilderScheduler.getInstance().queueStorageTask(() -> this.storageManager.updateRegion(region));
		this.regions.put(region.getName(), region);
		restartRebuildForRegionIfContinuous(region);
	}

	public void deleteRegion(final String name)
	{
		WorldRebuilderScheduler.getInstance().queueStorageTask(() -> this.storageManager.deleteRegion(name));
		WorldRebuilderScheduler.getInstance().cancelTasksForRegion(name);
		this.regions.remove(name);
	}

	public Region getRegion(final String name)
	{
		return this.regions.get(name);
	}

	public void forceRebuildRegion(final Region region)
	{
		WorldRebuilderScheduler worldRebuilderScheduler = WorldRebuilderScheduler.getInstance();
		List<WorldRebuilderTask> nonIntervalWorldRebuilderTasks = new LinkedList<>(worldRebuilderScheduler.getTasksForRegion(region.getName()));
		for (final WorldRebuilderTask worldRebuilderTask : nonIntervalWorldRebuilderTasks)
		{
			worldRebuilderScheduler.removeTaskForRegion(region.getName(), worldRebuilderTask);
			worldRebuilderTask.run();

			// Reschedule if rebuild is performed continuously
			if (region.getRebuildBlocksStrategy().doesRunContinuously())
			{
				region.rebuildBlocks(Collections.emptyList());
			}
		}
	}

	public void init() {
		this.storageManager.init();
	}

	private void startRebuildForContinuousRegions()
	{
		for (final Region region : getRegions())
		{
			restartRebuildForRegionIfContinuous(region);
		}
	}

	private void restartRebuildForRegionIfContinuous(Region region)
	{
		WorldRebuilderScheduler.getInstance().cancelTasksForRegion(region.getName());
		if (region.getRebuildBlocksStrategy().doesRunContinuously())
		{
			region.rebuildBlocks(Collections.emptyList());
			if (logger.isDebugEnabled())
			{
				logger.debug("Started continuous rebuild for region: {}", region.getName());
			}
		}
	}
}
