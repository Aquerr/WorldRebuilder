package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import io.github.aquerr.worldrebuilder.strategy.RebuildSameBlockStrategy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

public class CreateRegionCommand extends WRCommand
{

	public CreateRegionCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		final String name = context.requireOne(Parameter.string().key("name").build());

		if(!(context.cause().audience() instanceof ServerPlayer))
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(Component.text("Only in-game players can use this command!", NamedTextColor.RED)));

		final ServerPlayer player = (ServerPlayer)context.cause().audience();
		if (!super.getPlugin().getPlayerSelectionPoints().containsKey(player.uniqueId()))
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(Component.text("You must select two points in the world first before creating an arena!", NamedTextColor.RED)));

		final ServerWorld world = player.world();
		final SelectionPoints selectionPoints = super.getPlugin().getPlayerSelectionPoints().get(player.uniqueId());

		if (selectionPoints.getFirstPoint() == null || selectionPoints.getSecondPoint() == null)
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(Component.text("You must select two points in the world first before creating an arena!", NamedTextColor.RED)));

		if (super.getPlugin().getRegionManager().getRegion(name) != null)
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(Component.text("Region with such name already exists!", NamedTextColor.RED)));

		final Region region = new Region(name, world.uniqueId(), selectionPoints.getFirstPoint(), selectionPoints.getSecondPoint(), new RebuildSameBlockStrategy());
		super.getPlugin().getRegionManager().addRegion(region);
		player.sendMessage(WorldRebuilder.PLUGIN_PREFIX.append(Component.text("Region has been created!", NamedTextColor.GREEN)));
		return CommandResult.success();
	}
}
