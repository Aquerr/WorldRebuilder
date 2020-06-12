package io.github.aquerr.worldrebuilder.entity;

import com.flowpowered.math.vector.Vector3i;
import com.google.inject.internal.cglib.core.$ObjectSwitchCallback;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.util.RegionUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Region
{
	private final UUID worldUniqueId;
	private final Vector3i firstPoint;
	private final Vector3i secondPoint;

	private final String name;

	private boolean isActive;

	private int restoreTime; //Seconds. Default: 10

	private boolean shouldDropBlocks;

	// Position (x, y, z) --> Block Snapshot

	// Placed in the territory by mob/player after the region was created.
	private List<BlockSnapshot> blockSnapshotsExceptions;

	// Placed in the territory by mob/player after the region was created.
	private List<EntitySnapshot> entitySnapshotsException;

	public Region(final String name, final UUID worldUniqueId, final Vector3i firstPoint, final Vector3i secondPoint, final int restoreTime, final boolean isActive, final boolean shouldDropBlocks
		, final List<BlockSnapshot> blockSnapshotsExceptions, final List<EntitySnapshot> entitySnapshotsException)
	{
		this.name = name;
		this.worldUniqueId = worldUniqueId;
		this.firstPoint = firstPoint;
		this.secondPoint = secondPoint;

		this.restoreTime = restoreTime;
		this.isActive = isActive;
		this.shouldDropBlocks = shouldDropBlocks;

		this.blockSnapshotsExceptions = blockSnapshotsExceptions;
		this.entitySnapshotsException = entitySnapshotsException;

//		this.blockSnapshotsExceptions = RegionUtil.getBlockSnapshots(worldUniqueId, firstPoint, secondPoint);
//		this.entitySnapshotsException = RegionUtil.getEntitySnapshots(worldUniqueId, firstPoint, secondPoint);

	}

	public String getName()
	{
		return this.name;
	}

	public UUID getWorldUniqueId()
	{
		return this.worldUniqueId;
	}

	public Vector3i getFirstPoint()
	{
		return this.firstPoint;
	}

	public Vector3i getSecondPoint()
	{
		return this.secondPoint;
	}

	public boolean isActive()
	{
		return this.isActive;
	}

	public void setActive(final boolean value)
	{
		this.isActive = value;
	}

	public int getRestoreTime()
	{
		return this.restoreTime;
	}

	public void setRestoreTime(int restoreTimeInSeconds)
	{
		this.restoreTime = restoreTimeInSeconds;
	}

	public boolean shouldDropBlocks()
	{
		return this.shouldDropBlocks;
	}

	public void setShouldDropBlocks(boolean shouldDropBlocks)
	{
		this.shouldDropBlocks = shouldDropBlocks;
	}

	public boolean intersects(final UUID worldUniqueId, final Vector3i position)
	{
		if(!this.worldUniqueId.equals(worldUniqueId))
			return false;

		boolean intersectX = false;
		boolean intersectY = false;
		boolean intersectZ = false;

		//Check X
		if (this.firstPoint.getX() <= this.secondPoint.getX() && (position.getX() <= this.secondPoint.getX() && position.getX() >= this.firstPoint.getX()))
		{
			intersectX = true;
		}
		else if (this.firstPoint.getX() >= this.secondPoint.getX() && (position.getX() <= this.firstPoint.getX() && position.getX() >= this.secondPoint.getX()))
		{
			intersectX = true;
		}

		//Check Y
		if (this.firstPoint.getY() < this.secondPoint.getY() && (position.getY() <= this.secondPoint.getY() && position.getY() >= this.firstPoint.getY()))
		{
			intersectY = true;
		}
		else if (this.firstPoint.getY() >= this.secondPoint.getY() && (position.getY() <= this.firstPoint.getY() && position.getY() >= this.secondPoint.getY()))
		{
			intersectY = true;
		}

		//Check Z
		if (this.firstPoint.getZ() <= this.secondPoint.getZ() && (position.getZ() <= this.secondPoint.getZ() && position.getZ() >= this.firstPoint.getZ()))
		{
			intersectZ = true;
		}
		else if (this.firstPoint.getZ() >= this.secondPoint.getZ() && (position.getZ() <= this.firstPoint.getZ() && position.getZ() >= this.secondPoint.getZ()))
		{
			intersectZ = true;
		}

		return intersectX && intersectY && intersectZ;
	}

	public List<BlockSnapshot> getBlockSnapshotsExceptions()
	{
		return this.blockSnapshotsExceptions;
	}

	public List<EntitySnapshot> getEntitySnapshotsExceptions()
	{
		return this.entitySnapshotsException;
	}

	public boolean isEntityIgnored(final Entity entity)
	{
		for (final EntitySnapshot entitySnapshot : this.entitySnapshotsException)
		{
			if (entity.getLocation().getBlockPosition().equals(entitySnapshot.getPosition()) && entity.getType().equals(entitySnapshot.getType()))
			{
				CompletableFuture.runAsync(() -> {
					entitySnapshotsException.remove(entitySnapshot);
					WorldRebuilder.getPlugin().getRegionManager().updateRegion(this);
				});
				return true;
			}
		}
		return false;
	}

	public boolean isBlockIgnored(final BlockSnapshot blockSnapshot)
	{
		for (final BlockSnapshot blockException : this.getBlockSnapshotsExceptions())
		{
			if (blockException.getPosition().equals(blockSnapshot.getPosition()) && blockException.getState().getType().equals(blockSnapshot.getState().getType()))
			{
				CompletableFuture.runAsync(() -> {
					blockSnapshotsExceptions.remove(blockException);
					WorldRebuilder.getPlugin().getRegionManager().updateRegion(this);
				});
				return true;
			}
		}

		return false;
	}

//	public Map<Vector3i, BlockSnapshot> getBlockSnapshots()
//	{
//		return this.blockSnapshots;
//	}
//
//	public List<EntitySnapshot> getEntitySnapshots()
//	{
//		return this.entitySnapshots;
//	}
}
