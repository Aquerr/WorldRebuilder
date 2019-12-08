package io.github.aquerr.worldrebuilder.scheduling;

import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

@Singleton
public class WorldRebuilderScheduler
{
	private final WorldRebuilder plugin;
	private final Scheduler spongeScheduler;

	public WorldRebuilderScheduler(final WorldRebuilder plugin)
	{
		this.plugin = plugin;
		this.spongeScheduler = Sponge.getScheduler();
	}

	public void scheduleRebuildBlocksTask(final RebuildBlocksTask rebuildBlocksTask, final int delayInSeconds)
	{
		this.spongeScheduler.createTaskBuilder().execute(rebuildBlocksTask).delay(delayInSeconds, TimeUnit.SECONDS).name("World Rebuilder - Blocks Rebuild Task").submit(this.plugin);
	}
}
