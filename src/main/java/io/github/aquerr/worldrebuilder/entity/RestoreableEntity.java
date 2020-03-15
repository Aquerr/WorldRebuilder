package io.github.aquerr.worldrebuilder.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.ArmorStandData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.util.Direction;

import java.util.Optional;

public class RestoreableEntity
{
    private final EntitySnapshot entitySnapshot;
    private final DataContainer extraData;

    private RestoreableEntity(final EntitySnapshot entitySnapshot, DataContainer extraData)
    {
        this.entitySnapshot = entitySnapshot;
        this.extraData = extraData;
    }

    public DataContainer getExtraData()
    {
        return extraData;
    }

    public EntitySnapshot getEntitySnapshot()
    {
        return entitySnapshot;
    }

    public static RestoreableEntity of(final Entity entity)
    {
        final DataContainer extraData = DataContainer.createNew();

        if (entity instanceof Hanging)
        {
            final Optional<Direction> direction = entity.get(Keys.DIRECTION);
            if (direction.isPresent())
            {
                extraData.set(DataQuery.of("direction"), direction);
            }
        }
        else if (entity instanceof ArmorStand)
        {
            final Optional<ArmorStandData> optionalArmorStandData = entity.get(ArmorStandData.class);
            if (optionalArmorStandData.isPresent())
            {
                final ArmorStandData armorStandData = optionalArmorStandData.get();
                extraData.set(DataQuery.of(Keys.ARMOR_STAND_HAS_ARMS.getName()), armorStandData.arms());
                extraData.set(DataQuery.of(Keys.ARMOR_STAND_HAS_BASE_PLATE.getName()), armorStandData.basePlate());
                extraData.set(DataQuery.of(Keys.ARMOR_STAND_IS_SMALL.getName()), armorStandData.small());
                extraData.set(DataQuery.of(Keys.ARMOR_STAND_MARKER.getName()), armorStandData.marker());
            }
        }

        return new RestoreableEntity(entity.createSnapshot(), extraData);
    }
}
