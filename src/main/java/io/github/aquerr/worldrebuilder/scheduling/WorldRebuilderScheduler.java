package io.github.aquerr.worldrebuilder.scheduling;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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

		ScheduledTask scheduledTask = this.underlyingScheduler.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.execute(rebuildBlocksTask)
				.delay(delayInSeconds, TimeUnit.SECONDS)
				.build(), getNewTaskName());

		rebuildBlocksTask.setTask(scheduledTask);
		addTaskToList(rebuildBlocksTask);
	}

	public void scheduleRebuildEntitiesTask(final String regionName, final UUID worldUUID, List<EntitySnapshot> entities, final int delayInSeconds)
	{
		RebuildEntitiesTask rebuildEntitiesTask = new RebuildEntitiesTask(regionName, worldUUID, entities);

		ScheduledTask scheduledTask = this.underlyingScheduler.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.delay(delayInSeconds, TimeUnit.SECONDS)
				.execute(rebuildEntitiesTask)
				.build(), getNewTaskName());

		rebuildEntitiesTask.setTask(scheduledTask);
		addTaskToList(rebuildEntitiesTask);
	}

	public void scheduleRebuildEntityTask(String regionName, final UUID worldUUID, Entity entity, final int delayInSeconds)
	{
		RebuildEntityTask rebuildEntityTask = new RebuildEntityTask(regionName, worldUUID, entity);

		ScheduledTask scheduledTask = this.underlyingScheduler.submit(Task.builder()
				.execute(rebuildEntityTask)
				.delay(delayInSeconds, TimeUnit.SECONDS)
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.build(), getNewTaskName());

		rebuildEntityTask.setTask(scheduledTask);
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
