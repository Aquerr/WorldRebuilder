package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;

public class AbstractListener
{
	private final WorldRebuilder plugin;

	public AbstractListener(final WorldRebuilder plugin)
	{
		this.plugin = plugin;
	}

	public WorldRebuilder getPlugin()
	{
		return this.plugin;
	}
}
