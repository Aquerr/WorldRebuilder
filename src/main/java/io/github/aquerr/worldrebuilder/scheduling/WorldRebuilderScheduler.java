package io.github.aquerr.worldrebuilder.scheduling;

import com.google.inject.Inject;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorldRebuilderScheduler
{
	private static final ExecutorService STORAGE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
//	private static final ScheduledExecutorService HEARTBEAT_SERVICE = Executors.newSingleThreadScheduledExecutor();
	private static WorldRebuilderScheduler INSTANCE;

	private final Map<String, List<WorldRebuilderTask>> rebuildTasks = new ConcurrentHashMap<>();

	private final Scheduler underlyingScheduler;
	private final Logger logger;

	public static WorldRebuilderScheduler getInstance()
	{
		return INSTANCE;
	}

	@Inject
	public WorldRebuilderScheduler(final Scheduler scheduler, final Logger logger)
	{
		this.underlyingScheduler = scheduler;
		this.logger = logger;
		INSTANCE = this;
//		HEARTBEAT_SERVICE.scheduleAtFixedRate();
	}

	public void queueStorageTask(Runnable runnable)
	{
		STORAGE_EXECUTOR_SERVICE.submit(runnable);
	}

	public void scheduleTask(WorldRebuilderTask worldRebuilderTask)
	{
		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduling task: " + worldRebuilderTask);
		}

		ScheduledTask scheduledTask = this.underlyingScheduler.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.execute(worldRebuilderTask)
				.delay(worldRebuilderTask.getDelay(), TimeUnit.SECONDS)
				.build(), getNewTaskName());

		worldRebuilderTask.setTask(scheduledTask);
		addTaskToList(worldRebuilderTask);

		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduled task: " + worldRebuilderTask);
		}
	}

	public void scheduleIntervalTask(WorldRebuilderTask worldRebuilderTask)
	{
		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduling task: " + worldRebuilderTask);
		}

		ScheduledTask scheduledTask = this.underlyingScheduler.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.execute(worldRebuilderTask)
				.delay(1, TimeUnit.SECONDS)
				.interval(worldRebuilderTask.getDelay(), TimeUnit.SECONDS)
				.build(), getNewTaskName());

		worldRebuilderTask.setTask(scheduledTask);
		addTaskToList(worldRebuilderTask);

		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduled task: " + worldRebuilderTask);
		}
	}

	public Scheduler getUnderlyingScheduler()
	{
		return this.underlyingScheduler;
	}

	public List<WorldRebuilderTask> getTasksForRegion(final String regionName)
	{
		return Optional.ofNullable(this.rebuildTasks.get(regionName.toLowerCase()))
				.orElse(Collections.emptyList());
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
		if (logger.isDebugEnabled())
		{
			logger.debug("Cancel tasks for region: {}", regionName);
		}

		getTasksForRegion(regionName).forEach(WorldRebuilderTask::cancel);
		this.rebuildTasks.remove(regionName.toLowerCase());
	}

	public void removeTaskForRegion(String regionName, WorldRebuilderTask worldRebuilderTask)
	{
		if (logger.isDebugEnabled())
		{
			logger.debug("Removing task {} for region: {}", regionName, worldRebuilderTask);
		}

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
		if (logger.isDebugEnabled())
		{
			logger.debug("Removing tasks for region: {}", regionName);
		}

		regionName = regionName.toLowerCase();
		List<WorldRebuilderTask> tasks = this.rebuildTasks.getOrDefault(regionName, Collections.emptyList());
		for (WorldRebuilderTask task : tasks)
		{
			task.cancel();
		}
		this.rebuildTasks.remove(regionName);
	}
}
