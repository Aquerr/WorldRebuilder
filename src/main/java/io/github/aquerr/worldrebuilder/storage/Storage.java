package io.github.aquerr.worldrebuilder.storage;

import io.github.aquerr.worldrebuilder.entity.Region;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.List;

public interface Storage
{
	void reload();

	void addRegion(Region region) throws ObjectMappingException;

	void deleteRegion(String name);

	Region getRegion(String name) throws ObjectMappingException;

	List<Region> getRegions() throws ObjectMappingException;
}
