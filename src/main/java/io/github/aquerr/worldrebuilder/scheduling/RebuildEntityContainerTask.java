package io.github.aquerr.worldrebuilder.scheduling;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.trait.EnumTraits;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class RebuildEntityContainerTask implements Runnable
{
    private final UUID worldUUID;
    private final DataContainer dataContainer;

    public RebuildEntityContainerTask(final UUID worldUUID, final DataContainer dataContainer)
    {
        this.worldUUID = worldUUID;
        this.dataContainer = dataContainer;
    }

    @Override
    public void run()
    {
        final Optional<World> optionalWorld = Sponge.getServer().getWorld(worldUUID);
        if (!optionalWorld.isPresent())
            return;
        final World world = optionalWorld.get();

        final Optional<EntityType> entityType = Sponge.getRegistry()
                .getType(EntityType.class, (String) this.dataContainer.get(DataQuery.of("EntityType")).get());
        final DataView vector3d = (DataView) this.dataContainer.get(DataQuery.of("Position")).get();

        final double x = vector3d.getDouble(DataQuery.of("X")).get();
        final double y = vector3d.getDouble(DataQuery.of("Y")).get();
        final double z = vector3d.getDouble(DataQuery.of("Z")).get();
        final Vector3d position = new Vector3d(x, y, z);
        final Direction direction = Direction.SOUTH;

        if (entityType.isPresent())
        {
            final ItemFrame itemFrame = (ItemFrame) world.createEntity(entityType.get(), position);
            itemFrame.offer(Keys.DIRECTION, direction);
            final boolean spawned = world.spawnEntity(itemFrame);
            if (!spawned)
            {
                System.out.println("ERROR!");
            }
        }
    }
}
