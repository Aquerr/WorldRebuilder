package io.github.aquerr.worldrebuilder.scheduling;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RebuildEntitiesTask implements WorldRebuilderTask
{
	private final String regionName;
	private final UUID worldUUID;
	private final List<EntitySnapshot> entitySnapshots;
	private Task task;

	RebuildEntitiesTask(final String regionName, final UUID worldUUID, final List<EntitySnapshot> entitySnapshots)
	{
		this.regionName = regionName;
		this.worldUUID = worldUUID;
		this.entitySnapshots = entitySnapshots;
	}

	@Override
	public void run()
	{
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(worldUUID);
		if(!optionalWorld.isPresent())
			return;
		final World world = optionalWorld.get();

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
				.map(EntitySnapshot::getPosition)
				.collect(Collectors.toList());
	}

	@Override
	public UUID getWorldUniqueId()
	{
		return this.worldUUID;
	}

	@Override
	public boolean cancel()
	{
		return this.task.cancel();
	}

	public void setTask(Task task)
	{
		this.task = task;
	}
}
