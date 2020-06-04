package io.github.aquerr.worldrebuilder.util;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

public class RegionUtil
{
//    public static Map<Vector3i, BlockSnapshot> getBlockSnapshots(final UUID worldUUID, Vector3i firstCorner, Vector3i secondCorner)
//    {
//        final Optional<World> optionalWorld = Sponge.getServer().getWorld(worldUUID);
//        if (!optionalWorld.isPresent())
//            return new HashMap<>();
//        final World world = optionalWorld.get();
//        return getBlockSnapshots(world, firstCorner, secondCorner);
//    }
//
//    public static List<EntitySnapshot> getEntitySnapshots(final UUID worldUUID, final Vector3i firstCorner, final Vector3i secondCorner)
//    {
//        final Optional<World> optionalWorld = Sponge.getServer().getWorld(worldUUID);
//        if (!optionalWorld.isPresent())
//            return new ArrayList<>();
//        final World world = optionalWorld.get();
//        return getEntitySnapshots(world, firstCorner, secondCorner);
//    }
//
//    public static Map<Vector3i, BlockSnapshot> getBlockSnapshots(final World world, final Vector3i firstCorner, final Vector3i secondCorner)
//    {
//        final Map<Vector3i, BlockSnapshot> blockSnapshots = new HashMap<>();
//
//        int startX = Math.min(firstCorner.getX(), secondCorner.getX());
//        int startY = Math.min(firstCorner.getY(), secondCorner.getY());
//        int startZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
//
//        int endX = Math.max(firstCorner.getX(), secondCorner.getX()) + 1;
//        int endZ = Math.max(firstCorner.getZ(), secondCorner.getZ()) + 1;
//        int endY = Math.max(firstCorner.getY(), secondCorner.getY()) + 1;
//
//        for (int x = startX; x <= endX; x++)
//        {
//            for (int y = startY; y <=  endY; y++)
//            {
//                for (int z = startZ; z <= endZ; z++)
//                {
//                    final BlockSnapshot blockSnapshot = world.createSnapshot(x, y, z);
//                    if (blockSnapshot.getState().getType() == BlockTypes.AIR)
//                        continue;
//                    blockSnapshots.put(Vector3i.from(x, y, z), blockSnapshot);
//                }
//            }
//        }
//        return blockSnapshots;
//    }
//
//    public static List<EntitySnapshot> getEntitySnapshots(final World world, final Vector3i firstCorner, final Vector3i secondCorner)
//    {
//        final List<EntitySnapshot> entitySnapshots = new ArrayList<>();
//
//        int startX = Math.min(firstCorner.getX(), secondCorner.getX());
//        int startY = Math.min(firstCorner.getY(), secondCorner.getY());
//        int startZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
//
//        int endX = Math.max(firstCorner.getX(), secondCorner.getX()) + 1;
//        int endZ = Math.max(firstCorner.getZ(), secondCorner.getZ()) + 1;
//        int endY = Math.max(firstCorner.getY(), secondCorner.getY()) + 1;
//
//        for (final Entity entity : world.getEntities())
//        {
//            final Location<World> location = entity.getLocation();
//            final double entityX = location.getX();
//            final double entityY = location.getY();
//            final double entityZ = location.getZ();
//
//            if ((entityX >= startX && entityX <= endX) && (entityY >= startY && entityY <= endY) && (entityZ >= startZ && entityZ <= endZ))
//            {
//                entitySnapshots.add(entity.createSnapshot());
//            }
//        }
//        return entitySnapshots;
//    }
}
