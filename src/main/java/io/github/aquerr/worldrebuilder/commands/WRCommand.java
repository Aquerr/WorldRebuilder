package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.command.CommandExecutor;

public abstract class WRCommand implements CommandExecutor
{
	private final WorldRebuilder plugin;

	public WRCommand(final WorldRebuilder plugin)
	{
		this.plugin = plugin;
	}

	WorldRebuilder getPlugin()
	{
		return this.plugin;
	}
}
