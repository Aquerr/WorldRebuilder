package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.commands.args.WorldRebuilderCommandParameters;
import io.github.aquerr.worldrebuilder.model.Region;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class DeleteRegionCommand extends WRCommand
{
	public DeleteRegionCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		final Region region = context.requireOne(WorldRebuilderCommandParameters.region());

		super.getPlugin().getRegionManager().deleteRegion(region.getName());
		context.sendMessage(Identity.nil(), getPlugin().getMessageSource().resolveMessageWithPrefix("command.region.delete.region-deleted"));
		return CommandResult.success();
	}
}
