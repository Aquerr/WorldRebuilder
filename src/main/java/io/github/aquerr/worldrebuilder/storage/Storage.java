package io.github.aquerr.worldrebuilder.storage;

import io.github.aquerr.worldrebuilder.model.Region;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public interface Storage
{
	void reload();

	void addRegion(Region region) throws SerializationException;

	void deleteRegion(String name);

	Region getRegion(String name) throws SerializationException;

	List<Region> getRegions() throws SerializationException;

    void init();
}
