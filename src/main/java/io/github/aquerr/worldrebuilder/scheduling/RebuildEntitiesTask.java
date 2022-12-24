package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RebuildEntitiesTask implements WorldRebuilderTask
{
	private final String regionName;
	private final UUID worldUUID;
	private final List<EntitySnapshot> entitySnapshots;
	private ScheduledTask task;
	private int delay;

	RebuildEntitiesTask(final String regionName, final UUID worldUUID, final List<EntitySnapshot> entitySnapshots)
	{
		this.regionName = regionName;
		this.worldUUID = worldUUID;
		this.entitySnapshots = entitySnapshots;
	}

	@Override
	public void run()
	{
		Region region = WorldRebuilder.getPlugin().getRegionManager().getRegion(regionName);
		if (!region.isActive())
			return;

		final Optional<ServerWorld> optionalWorld = WorldUtils.getWorldByUUID(worldUUID);
		if(!optionalWorld.isPresent())
			return;
		final ServerWorld world = optionalWorld.get();

		for(final EntitySnapshot entitySnapshot : this.entitySnapshots)
		{
			final Optional<Entity> optionalEntity = entitySnapshot.restore();
			optionalEntity.ifPresent(world::spawnEntity); //Do we need this?
		}

		WorldRebuilderScheduler.getInstance().removeTaskForRegion(regionName, this);
	}

	@Override
	public String getRegionName()
	{
		return this.regionName;
	}

	@Override
	public List<Vector3i> getAffectedPositions()
	{
		return this.entitySnapshots.stream()
				.map(EntitySnapshot::position)
				.collect(Collectors.toList());
	}

	public void setTask(ScheduledTask task)
	{
		this.task = task;
	}

	@Override
	public ScheduledTask getTask()
	{
		return this.task;
	}

	@Override
	public int getDelay()
	{
		return this.delay;
	}

	@Override
	public void setDelay(int delayInSeconds)
	{
		this.delay = delayInSeconds;
	}
}
