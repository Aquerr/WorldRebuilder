package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import net.minecraft.entity.item.HangingEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntitySpawnListener extends AbstractListener
{
    public EntitySpawnListener(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Listener
    public void onEntitySpawn(final SpawnEntityEvent event)
    {
        // If it is WorldRebuilder that is spawning an entity then don't execute code below.
        final Cause cause = event.cause();
        if (cause.contains(Sponge.pluginManager().fromInstance(super.getPlugin()).get()))
            return;

        //DropItemEvent is handled in separate method.
        //However, this method is still triggered by broken armor stands.
        if (event instanceof DropItemEvent)
            return;

        final EventContext eventContext = event.context();
        final Object source = event.source();
        final boolean isPlayerPlace = eventContext.get(EventContextKeys.PLAYER_PLACE).isPresent();

        if (isPlayerPlace || source instanceof Player)
        {
            final Collection<Region> regions = new ArrayList<>(super.getPlugin().getRegionManager().getRegions());
            for (final Region region : regions)
            {
                if (!region.isActive())
                    continue;

                final List<Entity> entities = event.entities();
                for (final Entity entity : entities)
                {
                    if(!(entity instanceof HangingEntity) && !(entity instanceof ArmorStand))
                        continue;

                    if (region.intersects(entity.serverLocation().world().uniqueId(), entity.serverLocation().blockPosition()))
                    {
                        region.getEntitySnapshotsExceptions().add(entity.createSnapshot());
                        super.getPlugin().getRegionManager().updateRegion(region);
                    }
                }
            }
        }
    }
}
