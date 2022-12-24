package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildEntityTask;
import net.minecraft.entity.item.HangingEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.world.server.ServerLocation;

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
	public void onEntityDestruct(final DestructEntityEvent event)
	{
		//Used for item frames, paintings and similar.
		final Entity entity = event.entity();
		if(!(entity instanceof HangingEntity) && !(entity instanceof ArmorStand))
			return;

		CompletableFuture.runAsync(() -> tryRebuildEntity(entity.serverLocation().world().uniqueId(), event.entity()));
	}

	private void tryRebuildEntity(final UUID worldUUID, final Entity entity)
	{
		final ServerLocation location = entity.serverLocation();
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		Region affectedRegion = null;

		for(final Region region : regions)
		{
			if (!region.isActive())
				continue;

			if(!region.intersects(worldUUID, location.blockPosition()))
				continue;

			// If it is an entity that we should ignore, then return.
			if (region.isEntityIgnored(entity))
			{
				region.removeIgnoredEntity(entity);
				break;
			}

			if (region.intersects(worldUUID, entity.serverLocation().blockPosition()))
			{
				affectedRegion = region;
				break;
			}
		}

		if (affectedRegion == null)
			return;

		RebuildEntityTask rebuildEntityTask = new RebuildEntityTask(affectedRegion.getName(), worldUUID, entity);
		rebuildEntityTask.setDelay(affectedRegion.getRestoreTime());
		super.getPlugin().getWorldRebuilderScheduler().scheduleTask(rebuildEntityTask);
	}
}
