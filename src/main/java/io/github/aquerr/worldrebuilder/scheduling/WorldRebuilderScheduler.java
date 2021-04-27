package io.github.aquerr.worldrebuilder.scheduling;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class WorldRebuilderScheduler
{
	private static WorldRebuilderScheduler INSTANCE;

	// RegionName => [ScheduledRebuildTasks]
	private final Map<String, List<WorldRebuilderTask>> rebuildTasks = new ConcurrentHashMap<>();

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

	public void scheduleRebuildBlocksTask(final String regionName, final UUID worldUUID, List<BlockSnapshot> blocks, final int delayInSeconds)
	{
		RebuildBlocksTask rebuildBlocksTask = new RebuildBlocksTask(regionName, worldUUID, blocks);

		Task task = this.underlyingScheduler.createTaskBuilder()
				.execute(rebuildBlocksTask)
				.delay(delayInSeconds, TimeUnit.SECONDS)
				.name(getNewTaskName())
				.submit(WorldRebuilder.getPlugin());

		rebuildBlocksTask.setTask(task);
		addTaskToList(rebuildBlocksTask);
	}

	public void scheduleRebuildEntitiesTask(final String regionName, final UUID worldUUID, List<EntitySnapshot> entities, final int delayInSeconds)
	{
		RebuildEntitiesTask rebuildEntitiesTask = new RebuildEntitiesTask(regionName, worldUUID, entities);

		Task task = this.underlyingScheduler.createTaskBuilder()
				.execute(rebuildEntitiesTask)
				.delay(delayInSeconds, TimeUnit.SECONDS)
				.name(getNewTaskName())
				.submit(WorldRebuilder.getPlugin());

		rebuildEntitiesTask.setTask(task);
		addTaskToList(rebuildEntitiesTask);
	}

	public void scheduleRebuildEntityTask(String regionName, final UUID worldUUID, Entity entity, final int delayInSeconds)
	{
		RebuildEntityTask rebuildEntityTask = new RebuildEntityTask(regionName, worldUUID, entity);

		Task task = this.underlyingScheduler.createTaskBuilder()
				.execute(rebuildEntityTask)
				.delay(delayInSeconds, TimeUnit.SECONDS)
				.name(getNewTaskName())
				.submit(WorldRebuilder.getPlugin());

		rebuildEntityTask.setTask(task);
		addTaskToList(rebuildEntityTask);
	}

	public Scheduler getUnderlyingScheduler()
	{
		return this.underlyingScheduler;
	}

	public List<WorldRebuilderTask> getTasksForRegion(final String regionName)
	{
		return this.rebuildTasks.get(regionName.toLowerCase());
	}

	private void addTaskToList(WorldRebuilderTask task)
	{
		LinkedList<WorldRebuilderTask> list = new LinkedList<>();
		list.add(task);
		this.rebuildTasks.merge(task.getRegionName().toLowerCase(), list, (list1, list2) -> {
			list1.addAll(list2);
			return list1;
		});
	}

	private String getNewTaskName()
	{
		return "WorldRebuilder - Rebuild Task - " + UUID.randomUUID();
	}

	public void cancelTasksForRegion(String regionName)
	{
		getTasksForRegion(regionName).forEach(WorldRebuilderTask::cancel);
		this.rebuildTasks.remove(regionName.toLowerCase());
	}

	public void removeTaskForRegion(String regionName, WorldRebuilderTask worldRebuilderTask)
	{
		regionName = regionName.toLowerCase();
		List<WorldRebuilderTask> tasks = this.rebuildTasks.get(regionName);
		if (tasks != null)
		{
			tasks.remove(worldRebuilderTask);
			if (tasks.size() == 0)
				this.rebuildTasks.remove(regionName);
		}
	}
}
