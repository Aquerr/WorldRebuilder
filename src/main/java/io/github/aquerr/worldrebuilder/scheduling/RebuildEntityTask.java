package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ArtType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RebuildEntityTask implements WorldRebuilderTask
{
	private final String regionName;
	private final UUID worldUUID;
	private final Entity entity;
	private ScheduledTask task;
	private int delay;

	public RebuildEntityTask(String regionName, final UUID worldUUID, final Entity entity)
	{
		this.regionName = regionName;
		this.worldUUID = worldUUID;
		this.entity = entity;
	}

	@Override
	public void run()
	{
		Region region = WorldRebuilder.getPlugin().getRegionManager().getRegion(regionName);
		if (!region.isActive())
			return;

		//If not removed then return. We do not want to duplicate entities.
		if (!entity.isRemoved())
			return;

		final Optional<ServerWorld> optionalWorld = WorldUtils.getWorldByUUID(worldUUID);
		if (!optionalWorld.isPresent())
			return;
		final ServerWorld world = optionalWorld.get();

		final Entity newEntity = world.createEntity(this.entity.type(), entity.serverLocation().position());
		if (newEntity instanceof Hanging)
		{
			final Optional<Direction> directionalData = this.entity.get(Keys.DIRECTION);
			directionalData.ifPresent(direction -> newEntity.offer(Keys.DIRECTION, direction));

			if (newEntity instanceof Painting)
			{
				final Optional<ArtType> optionalArt = this.entity.get(Keys.ART_TYPE);
				optionalArt.ifPresent(art -> newEntity.offer(Keys.ART_TYPE, art));
			}
		}
		else if (newEntity instanceof ArmorStand)
		{
			final ArmorStand oldArmorStand = (ArmorStand) this.entity;
			final ArmorStand newArmorStand = (ArmorStand) newEntity;

			newArmorStand.copyFrom(oldArmorStand);

//			final DataContainer dataContainer = oldArmorStand.getArmorStandData().toContainer();
//			final DataContainer dataContainer1 = oldArmorStand.getBodyPartRotationalData().toContainer();
//			final DataContainer dataContainer2 = oldArmorStand.getDamageableData().toContainer();
//			final DataContainer dataContainer3 = oldArmorStand.getHealthData().toContainer();
//
//			newArmorStand.offer(Sponge.getDataManager().getBuilder(ArmorStandData.class).get().build(dataContainer).get());
//			newArmorStand.offer(Sponge.getDataManager().getBuilder(BodyPartRotationalData.class).get().build(dataContainer1).get());
//			newArmorStand.offer(Sponge.getDataManager().getBuilder(DamageableData.class).get().build(dataContainer2).get());
//			newArmorStand.offer(Sponge.getDataManager().getBuilder(HealthData.class).get().build(dataContainer3).get());
//
//			final Vector3d rotation = oldArmorStand.getRotation();
//			newArmorStand.setRotation(rotation);
//
//			final Optional<Direction> directionalData = oldArmorStand.get(Keys.DIRECTION);
//			directionalData.ifPresent(direction -> newArmorStand.offer(Keys.DIRECTION, direction));
//
//			final ArmorStandData armorStandData = oldArmorStand.getArmorStandData();
//			newArmorStand.offer(armorStandData);
		}

		boolean didSpawn = world.spawnEntity(newEntity);
		if (!didSpawn)
		{
			WorldRebuilder.getPlugin().getLogger().error(WorldRebuilder.PLUGIN_PREFIX_PLAIN + "Could not restore entity: " + entity);
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
		return Collections.singletonList(this.entity.serverLocation().blockPosition());
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
