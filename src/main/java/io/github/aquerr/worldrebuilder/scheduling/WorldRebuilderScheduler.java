package io.github.aquerr.worldrebuilder.scheduling;

import com.google.inject.Inject;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorldRebuilderScheduler
{
	private static final ExecutorService SINGLE_THREADED_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
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

	public void queueStorageTask(Runnable runnable)
	{
		SINGLE_THREADED_EXECUTOR_SERVICE.submit(runnable);
	}

	public void scheduleTask(WorldRebuilderTask worldRebuilderTask)
	{
		ScheduledTask scheduledTask = this.underlyingScheduler.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.execute(worldRebuilderTask)
				.delay(worldRebuilderTask.getDelay(), TimeUnit.SECONDS)
				.build(), getNewTaskName());

		worldRebuilderTask.setTask(scheduledTask);
		addTaskToList(worldRebuilderTask);
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
		worldRebuilderTask.cancel();
		regionName = regionName.toLowerCase();
		List<WorldRebuilderTask> tasks = this.rebuildTasks.get(regionName);
		if (tasks != null)
		{
			tasks.remove(worldRebuilderTask);
			if (tasks.size() == 0)
				this.rebuildTasks.remove(regionName);
		}
	}

	public void removeTasksForRegion(String regionName)
	{
		regionName = regionName.toLowerCase();
		List<WorldRebuilderTask> tasks = this.rebuildTasks.getOrDefault(regionName, Collections.emptyList());
		for (WorldRebuilderTask task : tasks)
		{
			task.cancel();
		}
		this.rebuildTasks.remove(regionName);
	}
}
