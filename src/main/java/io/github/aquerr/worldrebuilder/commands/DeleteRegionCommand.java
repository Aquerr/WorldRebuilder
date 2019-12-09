package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class DeleteRegionCommand extends WRCommand
{
	public DeleteRegionCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(final CommandSource source, CommandContext args) throws CommandException
	{
		final Region region = args.requireOne(Text.of("region"));

		super.getPlugin().getRegionManager().deleteRegion(region.getName());
		source.sendMessage(Text.of(WorldRebuilder.PLUGIN_PREFIX, TextColors.GREEN, "Region has been deleted!"));
		return CommandResult.success();
	}
}
