package io.github.aquerr.worldrebuilder.scheduling;

import com.flowpowered.math.vector.Vector3d;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.data.manipulator.mutable.entity.BodyPartRotationalData;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class RebuildEntityTask implements Runnable
{
	private final UUID worldUUID;
	private final Entity entity;

	public RebuildEntityTask(final UUID worldUUID, final Entity entity)
	{
		this.worldUUID = worldUUID;
		this.entity = entity;
	}

	@Override
	public void run()
	{
		//If not removed then return. We do not want to duplicate entities.
		if (!entity.isRemoved())
			return;

		final Optional<World> optionalWorld = Sponge.getServer().getWorld(worldUUID);
		if(!optionalWorld.isPresent())
			return;
		final World world = optionalWorld.get();

		final Entity newEntity = world.createEntity(this.entity.getType(), entity.getLocation().getPosition());
		if (newEntity instanceof Hanging)
		{
			final Optional<Direction> directionalData = this.entity.get(Keys.DIRECTION);
			directionalData.ifPresent(direction -> newEntity.offer(Keys.DIRECTION, direction));

			if (newEntity instanceof Painting)
			{
				final Optional<Art> optionalArt = this.entity.get(Keys.ART);
				optionalArt.ifPresent(art -> newEntity.offer(Keys.ART, art));
			}
		}
		else if (newEntity instanceof ArmorStand)
		{
			final Vector3d rotation = this.entity.getRotation();
			newEntity.setRotation(rotation);

			final Optional<Direction> directionalData = this.entity.get(Keys.DIRECTION);
			directionalData.ifPresent(direction -> newEntity.offer(Keys.DIRECTION, direction));

			final Optional<ArmorStandData> optionalArmorStandData = this.entity.get(ArmorStandData.class);
			if (optionalArmorStandData.isPresent())
			{
				final ArmorStandData armorStandData = optionalArmorStandData.get();
				newEntity.offer(armorStandData);
			}

			final Optional<BodyPartRotationalData> optionalBodyPartRotationalData = this.entity.get(BodyPartRotationalData.class);
			if (optionalBodyPartRotationalData.isPresent())
			{
				final BodyPartRotationalData bodyPartRotationalData = optionalBodyPartRotationalData.get();
				newEntity.offer(bodyPartRotationalData);
			}
		}

		boolean didSpawn = world.spawnEntity(newEntity);
		if (!didSpawn)
		{
			Sponge.getServer().getConsole().sendMessage(WorldRebuilder.PLUGIN_ERROR.concat(Text.of("Could not restore entity " + entity)));
		}
	}
}
