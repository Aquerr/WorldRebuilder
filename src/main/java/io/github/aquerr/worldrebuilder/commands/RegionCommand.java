package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class RegionCommand extends WRCommand
{
	public RegionCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		return CommandResult.success();
	}
}
