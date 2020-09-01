package io.github.aquerr.worldrebuilder.scheduling;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.TimeUnit;

@Singleton
public class WorldRebuilderScheduler
{
	private static WorldRebuilderScheduler INSTANCE;

	private final Scheduler underlyingScheduler;

	public static WorldRebuilderScheduler getInstance()
	{
		return INSTANCE;
	}

	@Inject
	public WorldRebuilderScheduler(final Scheduler scheduler)
	{
		this.underlyingScheduler = scheduler;
		INSTANCE = this;
	}

	public void scheduleRebuildBlocksTask(final RebuildBlocksTask rebuildBlocksTask, final int delayInSeconds)
	{
		this.underlyingScheduler.createTaskBuilder().execute(rebuildBlocksTask).delay(delayInSeconds, TimeUnit.SECONDS).name("World Rebuilder - Blocks Rebuild Task").submit(WorldRebuilder.getPlugin());
	}

	public void scheduleRebuildEntityTask(final RebuildEntitiesTask rebuildEntitiesTask, final int delayInSeconds)
	{
		this.underlyingScheduler.createTaskBuilder().execute(rebuildEntitiesTask).delay(delayInSeconds, TimeUnit.SECONDS).name("World Rebuilder - Entities Rebuild Task").submit(WorldRebuilder.getPlugin());
	}

	public void scheduleRebuildEntityTask(final RebuildEntityTask rebuildEntityTask, final int delayInSeconds)
	{
		this.underlyingScheduler.createTaskBuilder().execute(rebuildEntityTask).delay(delayInSeconds, TimeUnit.SECONDS).name("World Rebuilder - Entity Rebuild Task").submit(WorldRebuilder.getPlugin());
	}

	public Scheduler getUnderlyingScheduler()
	{
		return this.underlyingScheduler;
	}
}
