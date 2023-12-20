package io.github.aquerr.worldrebuilder.scheduling;

import com.google.inject.Inject;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorldRebuilderScheduler
{
	private static final ExecutorService STORAGE_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
	private static WorldRebuilderScheduler INSTANCE;

	private final Map<String, List<WorldRebuilderTask>> rebuildTasks = new ConcurrentHashMap<>();

	private final Scheduler asyncScheduler;
	private final Scheduler syncScheduler;
	private final Logger logger;


	public static WorldRebuilderScheduler getInstance()
	{
		return INSTANCE;
	}

	@Inject
	public WorldRebuilderScheduler(final Scheduler scheduler, final Scheduler asyncScheduler, final Logger logger)
	{
		this.syncScheduler = scheduler;
		this.asyncScheduler = asyncScheduler;
		this.logger = logger;
		INSTANCE = this;
	}

	public void queueStorageTask(Runnable runnable)
	{
		STORAGE_EXECUTOR_SERVICE.submit(runnable);
	}

	public void scheduleTask(WorldRebuilderTask worldRebuilderTask)
	{
		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduling task: {}", worldRebuilderTask);
		}

		ScheduledTask scheduledTask = this.syncScheduler.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.execute(worldRebuilderTask)
				.delay(worldRebuilderTask.getDelay() * 1000L, ChronoUnit.MILLIS)
				.build());

		worldRebuilderTask.setTask(scheduledTask);
		addTaskToList(worldRebuilderTask);

		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduled task: {}", worldRebuilderTask);
		}
	}

	public void scheduleIntervalTaskAsync(WorldRebuilderTask worldRebuilderTask)
	{
		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduling task: {}", worldRebuilderTask);
		}

		ScheduledTask scheduledTask = this.asyncScheduler.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.execute(worldRebuilderTask)
				.interval(1, ChronoUnit.SECONDS)
				.build());

		worldRebuilderTask.setTask(scheduledTask);
		addTaskToList(worldRebuilderTask);

		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduled task: {}", worldRebuilderTask);
		}
	}

	public void scheduleTask(Runnable runnable)
	{
		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduling task: {}", runnable);
		}

		this.syncScheduler.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.execute(runnable)
				.build());

		if (this.logger.isDebugEnabled())
		{
			this.logger.debug("Scheduled task: {}", runnable);
		}
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
			if (tasks.isEmpty())
				this.rebuildTasks.remove(regionName);
		}
	}
}
