package io.github.aquerr.worldrebuilder.scheduling;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.data.manipulator.mutable.entity.BodyPartRotationalData;
import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import java.util.*;

public class RebuildEntityTask implements WorldRebuilderTask
{
	private final String regionName;
	private final UUID worldUUID;
	private final Entity entity;
	private Task task;

	RebuildEntityTask(String regionName, final UUID worldUUID, final Entity entity)
	{
		this.regionName = regionName;
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
		if (!optionalWorld.isPresent())
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
			final ArmorStand oldArmorStand = (ArmorStand) this.entity;
			final ArmorStand newArmorStand = (ArmorStand) newEntity;

			final DataContainer dataContainer = oldArmorStand.getArmorStandData().toContainer();
			final DataContainer dataContainer1 = oldArmorStand.getBodyPartRotationalData().toContainer();
			final DataContainer dataContainer2 = oldArmorStand.getDamageableData().toContainer();
			final DataContainer dataContainer3 = oldArmorStand.getHealthData().toContainer();

			newArmorStand.offer(Sponge.getDataManager().getBuilder(ArmorStandData.class).get().build(dataContainer).get());
			newArmorStand.offer(Sponge.getDataManager().getBuilder(BodyPartRotationalData.class).get().build(dataContainer1).get());
			newArmorStand.offer(Sponge.getDataManager().getBuilder(DamageableData.class).get().build(dataContainer2).get());
			newArmorStand.offer(Sponge.getDataManager().getBuilder(HealthData.class).get().build(dataContainer3).get());

			final Vector3d rotation = oldArmorStand.getRotation();
			newArmorStand.setRotation(rotation);

			final Optional<Direction> directionalData = oldArmorStand.get(Keys.DIRECTION);
			directionalData.ifPresent(direction -> newArmorStand.offer(Keys.DIRECTION, direction));

			final ArmorStandData armorStandData = oldArmorStand.getArmorStandData();
			newArmorStand.offer(armorStandData);
		}

		boolean didSpawn = world.spawnEntity(newEntity);
		if (!didSpawn)
		{
			Sponge.getServer().getConsole().sendMessage(WorldRebuilder.PLUGIN_ERROR.concat(Text.of("Could not restore entity " + entity)));
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
		return Collections.singletonList(this.entity.getLocation().getBlockPosition());
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
