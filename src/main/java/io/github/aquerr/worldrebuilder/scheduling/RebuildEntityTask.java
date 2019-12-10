package io.github.aquerr.worldrebuilder.scheduling;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class RebuildEntityTask implements Runnable
{
	private final UUID worldUUID;
	private final EntitySnapshot entitySnapshot;

	public RebuildEntityTask(final UUID worldUUID, final EntitySnapshot entitySnapshot)
	{
		this.worldUUID = worldUUID;
		this.entitySnapshot = entitySnapshot;
	}

	@Override
	public void run()
	{
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(worldUUID);
		if(!optionalWorld.isPresent())
			return;
		final World world = optionalWorld.get();
		final Optional<Entity> optionalEntity = entitySnapshot.restore();
		if(!optionalEntity.isPresent())
			return;
//		final Entity entity = optionalEntity.get();
		final Entity newEntity = world.createEntity(this.entitySnapshot.getType(), entitySnapshot.getLocation().get().getPosition());
		world.spawnEntity(newEntity);
	}

//	private Vector3d getPaintingLocation(final Vector3d blockPosition, final Direction direction)
//	{
//		switch(direction)
//		{
//			case EAST:
//				return blockPosition.add(0.1, 0, 0);
//			case WEST:
//				return blockPosition.add(-0.1, 0, 0);
//			case NORTH:
//				return blockPosition.add(0, 0, -0.1);
//			case SOUTH:
//				return blockPosition.add(0.1, 0, 0.1);
//		}
//
//		return Vector3d.ZERO;
//	}
}
