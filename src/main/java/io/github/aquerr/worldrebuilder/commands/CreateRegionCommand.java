package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

public class CreateRegionCommand extends WRCommand
{

	public CreateRegionCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
	{
		final String name = args.requireOne(Text.of("name"));

		if(!(source instanceof Player))
			throw new CommandException(Text.of(WorldRebuilder.PLUGIN_ERROR, TextColors.RED, "Only in-game players can use this command!"));

		final Player player = (Player)source;
		if (!super.getPlugin().getPlayerSelectionPoints().containsKey(player.getUniqueId()))
			throw new CommandException(Text.of(WorldRebuilder.PLUGIN_ERROR, TextColors.RED, "You must select two points in the world first before creating an arena!"));

		final World world = player.getWorld();
		final SelectionPoints selectionPoints = super.getPlugin().getPlayerSelectionPoints().get(player.getUniqueId());

		if (selectionPoints.getFirstPoint() == null || selectionPoints.getSecondPoint() == null)
			throw new CommandException(Text.of(WorldRebuilder.PLUGIN_ERROR, TextColors.RED, "You must select two points in the world first before creating an arena!"));

		final Region region = new Region(name, world.getUniqueId(), selectionPoints.getFirstPoint(), selectionPoints.getSecondPoint());
		super.getPlugin().getRegionManager().getRegions().put(name, region);
		player.sendMessage(Text.of(WorldRebuilder.PLUGIN_PREFIX, TextColors.GREEN, "Region has been created!"));
		return CommandResult.success();
	}
}
