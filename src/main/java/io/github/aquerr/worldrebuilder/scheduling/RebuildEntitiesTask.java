package io.github.aquerr.worldrebuilder.scheduling;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RebuildEntitiesTask implements Runnable
{
	private final UUID worldUUID;
	private final List<EntitySnapshot> entitySnapshots;

	public RebuildEntitiesTask(final UUID worldUUID, final List<EntitySnapshot> entitySnapshots)
	{
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
	}
}
