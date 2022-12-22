package io.github.aquerr.worldrebuilder.entity;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.strategy.RebuildBlocksStrategy;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

	private RebuildBlocksStrategy rebuildBlocksStrategy;

	public Region(final String name, final UUID worldUniqueId, final Vector3i firstPoint, final Vector3i secondPoint, int restoreTime, RebuildBlocksStrategy rebuildBlocksStrategy)
	{
		this(name, worldUniqueId, firstPoint, secondPoint, restoreTime, true, true, new ArrayList<>(), new ArrayList<>(), rebuildBlocksStrategy);
	}

	public Region(final String name, final UUID worldUniqueId, final Vector3i firstPoint,
				  final Vector3i secondPoint, final int restoreTime,
				  final boolean isActive, final boolean shouldDropBlocks,
				  final List<BlockSnapshot> blockSnapshotsExceptions, final List<EntitySnapshot> entitySnapshotsException,
				  RebuildBlocksStrategy rebuildBlocksStrategy)
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

		this.rebuildBlocksStrategy = rebuildBlocksStrategy;

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

	public void setRebuildBlocksStrategy(RebuildBlocksStrategy rebuildBlocksStrategy)
	{
		this.rebuildBlocksStrategy = rebuildBlocksStrategy;
	}

	public RebuildBlocksStrategy getRebuildBlocksStrategy()
	{
		return rebuildBlocksStrategy;
	}

	public boolean intersects(final UUID worldUniqueId, final Vector3i position)
	{
		if(!this.worldUniqueId.equals(worldUniqueId))
			return false;

		boolean intersectX = false;
		boolean intersectY = false;
		boolean intersectZ = false;

		//Check X
		if (this.firstPoint.x() <= this.secondPoint.x() && (position.x() <= this.secondPoint.x() && position.x() >= this.firstPoint.x()))
		{
			intersectX = true;
		}
		else if (this.firstPoint.x() >= this.secondPoint.x() && (position.x() <= this.firstPoint.x() && position.x() >= this.secondPoint.x()))
		{
			intersectX = true;
		}

		//Check Y
		if (this.firstPoint.y() < this.secondPoint.y() && (position.y() <= this.secondPoint.y() && position.y() >= this.firstPoint.y()))
		{
			intersectY = true;
		}
		else if (this.firstPoint.y() >= this.secondPoint.y() && (position.y() <= this.firstPoint.y() && position.y() >= this.secondPoint.y()))
		{
			intersectY = true;
		}

		//Check Z
		if (this.firstPoint.z() <= this.secondPoint.z() && (position.z() <= this.secondPoint.z() && position.z() >= this.firstPoint.z()))
		{
			intersectZ = true;
		}
		else if (this.firstPoint.z() >= this.secondPoint.z() && (position.z() <= this.firstPoint.z() && position.z() >= this.secondPoint.z()))
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
			if (entity.location().blockPosition().equals(entitySnapshot.position()) && entity.type().equals(entitySnapshot.type()))
			{
				return true;
			}
		}
		return false;
	}

	public void removeIgnoredEntity(final Entity entity)
	{
		CompletableFuture.runAsync(() -> {
			EntitySnapshot entityToRemove = null;
			for (final EntitySnapshot entitySnapshot : this.entitySnapshotsException)
			{
				if (entity.location().blockPosition().equals(entitySnapshot.position()) && entity.type().equals(entitySnapshot.type()))
				{
					entityToRemove = entitySnapshot;
					break;
				}
			}
			if (entityToRemove == null)
				return;

			entitySnapshotsException.remove(entityToRemove);
			WorldRebuilder.getPlugin().getRegionManager().updateRegion(this);
		});
	}

	public boolean isBlockIgnored(final BlockSnapshot blockSnapshot)
	{
		for (final BlockSnapshot blockException : this.getBlockSnapshotsExceptions())
		{
			if (blockException.position().equals(blockSnapshot.position()) && blockException.state().type().equals(blockSnapshot.state().type()))
			{
				return true;
			}
		}
		return false;
	}

	public void removeIgnoredBlock(final BlockSnapshot blockSnapshot)
	{
		Sponge.game().asyncScheduler()
				.submit(Task.builder()
				.plugin(WorldRebuilder.getPlugin().getPluginContainer())
				.delay(2, TimeUnit.SECONDS)
						.execute(() ->
						{
							BlockSnapshot blockToRemove = null;
							for (final BlockSnapshot blockException : getBlockSnapshotsExceptions())
							{
								if (blockException.position().equals(blockSnapshot.position()) && blockException.state().type().equals(blockSnapshot.state().type()))
								{
									blockToRemove = blockException;
									break;
								}
							}
							if (blockToRemove == null)
								return;

							blockSnapshotsExceptions.remove(blockToRemove);
							WorldRebuilder.getPlugin().getRegionManager().updateRegion(this);
						})
				.build());
	}

	public void rebuildBlocks(List<BlockSnapshot> blockSnapshots)
	{
		if (!this.isActive)
			return;

		final List<BlockSnapshot> blocksToRestore = new ArrayList<>();
		if (!getRebuildBlocksStrategy().doesRunContinuously())
		{
			for (final BlockSnapshot blockSnapshot : blockSnapshots)
			{
				if(canRestoreBlock(blockSnapshot))
				{
					blocksToRestore.add(blockSnapshot);
				}
			}

			if (blocksToRestore.isEmpty())
				return;
		}
		this.rebuildBlocksStrategy.rebuildBlocks(this, blocksToRestore);
	}

	private boolean canRestoreBlock(BlockSnapshot blockSnapshot)
	{
		return blockSnapshot.location().filter(this::canRestoreBlockSnapshotLocation).isPresent() && !isBlockIgnored(blockSnapshot);
	}

	private boolean canRestoreBlockSnapshotLocation(ServerLocation serverLocation)
	{
		return intersects(serverLocation.world().uniqueId(), serverLocation.blockPosition());
	}
}
