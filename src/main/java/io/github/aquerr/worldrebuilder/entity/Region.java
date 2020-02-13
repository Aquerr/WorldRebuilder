package io.github.aquerr.worldrebuilder.entity;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;

public class Region
{
	private final UUID worldUniqueId;
	private final Vector3i firstPoint;
	private final Vector3i secondPoint;

	private final String name;

	private boolean isActive;

	private int restoreTime; //Seconds. Default: 10

	public Region(final String name, final UUID worldUniqueId, final Vector3i firstPoint, final Vector3i secondPoint, final int restoreTime, final boolean isActive)
	{
		this.name = name;
		this.worldUniqueId = worldUniqueId;
		this.firstPoint = firstPoint;
		this.secondPoint = secondPoint;
		this.restoreTime = restoreTime;
		this.isActive = isActive;
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
}
