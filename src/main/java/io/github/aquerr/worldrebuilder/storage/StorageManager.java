package io.github.aquerr.worldrebuilder.storage;

import com.google.inject.Singleton;
import io.github.aquerr.worldrebuilder.WorldRebuilder;

@Singleton
public class StorageManager
{
	private final WorldRebuilder plugin;
	private final Storage storage;

	public StorageManager(final WorldRebuilder plugin)
	{
		this.plugin = plugin;
		this.storage = new HOCONStorage(plugin);
	}
}
