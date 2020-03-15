package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildEntityTask;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntityDestroyListener extends AbstractListener
{
	public EntityDestroyListener(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Listener
	public void onEntityAttacked(final AttackEntityEvent event)
	{
		//Used for item frames, paintings and similar.
		final Entity entity = event.getTargetEntity();

		if(!(entity instanceof Hanging) && !(entity instanceof ArmorStand))
			return;

//		CompletableFuture.runAsync(() -> tryRebuildEntity(entity.getWorld().getUniqueId(), event.getTargetEntity().createSnapshot()));
		CompletableFuture.runAsync(() -> tryRebuildEntity(entity.getWorld().getUniqueId(), event.getTargetEntity()));
	}

	private void tryRebuildEntity(final UUID worldUUID, final Entity entity)
	{
		final Location<World> location = entity.getLocation();
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		Region affectedRegion = null;

		for(final Region region : regions)
		{
			if (!region.isActive())
				continue;

			if(region.intersects(worldUUID, location.getBlockPosition()))
			{
				affectedRegion = region;
				break;
			}
		}

		if (affectedRegion == null)
			return;

		super.getPlugin().getWorldRebuilderScheduler().scheduleRebuildEntityTask(new RebuildEntityTask(worldUUID, entity), affectedRegion.getRestoreTime());
	}

//	private void tryRebuildEntity(final UUID worldUUID, final EntitySnapshot entity)
//	{
//		final Optional<Location<World>> optionalLocation = entity.getLocation();
//		if(!optionalLocation.isPresent())
//			return;
//		final Location<World> location = optionalLocation.get();
//
//		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
//		Region affectedRegion = null;
//
//		for(final Region region : regions)
//		{
//			if (!region.isActive())
//				continue;
//
//			if(region.intersects(worldUUID, location.getBlockPosition()))
//			{
//				affectedRegion = region;
//				break;
//			}
//		}
//
//		if (affectedRegion == null)
//			return;
//
//		super.getPlugin().getWorldRebuilderScheduler().scheduleRebuildEntityTask(new RebuildEntityTask(worldUUID, entity), affectedRegion.getRestoreTime());
//	}
}
