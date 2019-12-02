package io.github.aquerr.worldrebuilder.managers;

import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.entity.Region;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class RegionManager
{
	private final Map<String, Region> regions = new HashMap<>();

	public Map<String, Region> getRegions()
	{
		return this.regions;
	}
}
