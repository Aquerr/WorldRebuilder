package io.github.aquerr.worldrebuilder.entity;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;

public class Region
{
	private final UUID worldUniqueId;
	private final Vector3i firstPoint;
	private final Vector3i secondPoint;

	private final String name;

	public Region(final String name, final UUID worldUniqueId, final Vector3i firstPoint, final Vector3i secondPoint)
	{
		this.name = name;
		this.worldUniqueId = worldUniqueId;
		this.firstPoint = firstPoint;
		this.secondPoint = secondPoint;
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

	public boolean intersects(final Vector3i position)
	{
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
