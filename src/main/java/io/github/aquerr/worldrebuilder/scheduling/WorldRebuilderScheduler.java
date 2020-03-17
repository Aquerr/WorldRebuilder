package io.github.aquerr.worldrebuilder.scheduling;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

@Singleton
public class WorldRebuilderScheduler
{
	private final Scheduler underlayingScheduler;

	@Inject
	public WorldRebuilderScheduler(final Scheduler scheduler)
	{
		this.underlayingScheduler = scheduler;
	}

	public void scheduleRebuildBlocksTask(final RebuildBlocksTask rebuildBlocksTask, final int delayInSeconds)
	{
		this.underlayingScheduler.createTaskBuilder().execute(rebuildBlocksTask).delay(delayInSeconds, TimeUnit.SECONDS).name("World Rebuilder - Blocks Rebuild Task").submit(WorldRebuilder.getPlugin());
	}

	public void scheduleRebuildEntityTask(final RebuildEntitiesTask rebuildEntitiesTask, final int delayInSeconds)
	{
		this.underlayingScheduler.createTaskBuilder().execute(rebuildEntitiesTask).delay(delayInSeconds, TimeUnit.SECONDS).name("World Rebuilder - Entities Rebuild Task").submit(WorldRebuilder.getPlugin());
	}

	public void scheduleRebuildEntityTask(final RebuildEntityTask rebuildEntityTask, final int delayInSeconds)
	{
		this.underlayingScheduler.createTaskBuilder().execute(rebuildEntityTask).delay(delayInSeconds, TimeUnit.SECONDS).name("World Rebuilder - Entity Rebuild Task").submit(WorldRebuilder.getPlugin());
	}
}
