package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import io.github.aquerr.worldrebuilder.strategy.RebuildBlockFromRandomBlockSetInIntervalStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildBlockFromRandomBlockSetStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildInIntervalStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildSameBlockStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyType;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;

import static net.kyori.adventure.text.Component.text;

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
		final RebuildStrategyType strategyType = context.one(Parameter.enumValue(RebuildStrategyType.class).key("strategyType").build()).orElse(RebuildStrategyType.SAME_BLOCK);

		final Collection<? extends BlockState> blockList = context.all(Parameter.blockState().key("blockList").build());

		if (!(context.cause().audience() instanceof ServerPlayer))
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(text("Only in-game players can use this command!", NamedTextColor.RED)));

		final ServerPlayer player = (ServerPlayer) context.cause().audience();
		if (!super.getPlugin().getPlayerSelectionPoints().containsKey(player.uniqueId()))
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(text("You must select two points in the world first before creating an arena!", NamedTextColor.RED)));

		final ServerWorld world = player.world();
		final SelectionPoints selectionPoints = super.getPlugin().getPlayerSelectionPoints().get(player.uniqueId());

		if (selectionPoints.getFirstPoint() == null || selectionPoints.getSecondPoint() == null)
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(text("You must select two points in the world first before creating an arena!", NamedTextColor.RED)));

		if (super.getPlugin().getRegionManager().getRegion(name) != null)
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(text("Region with such name already exists!", NamedTextColor.RED)));

		Region region = createRegionForSelectedStrategy(name, strategyType, blockList, world, selectionPoints);
		super.getPlugin().getRegionManager().addRegion(region);
		player.sendMessage(WorldRebuilder.PLUGIN_PREFIX.append(text("Region has been created!", NamedTextColor.GREEN)));
		return CommandResult.success();
	}

	private Region createRegionForSelectedStrategy(String regionName, RebuildStrategyType rebuildStrategyType, Collection<? extends BlockState> blocks, ServerWorld world, SelectionPoints selectionPoints) throws CommandException
	{
		if (rebuildStrategyType.hasPredefinedBlockSet() && blocks == null)
		{
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(text("Selected rebuild strategy require predefined block set!")));
		}

		try
		{
			switch (rebuildStrategyType)
			{
				case RANDOM_BLOCK_FROM_SET:
					return new Region(regionName, world.uniqueId(), selectionPoints.getFirstPoint(), selectionPoints.getSecondPoint(), 10, new RebuildBlockFromRandomBlockSetStrategy(new ArrayList<>(blocks)));
				case CONSTANT_REBUILD_IN_INTERVAL_RANDOM_BLOCK_FROM_SET:
					return new Region(regionName, world.uniqueId(), selectionPoints.getFirstPoint(), selectionPoints.getSecondPoint(), 60, new RebuildBlockFromRandomBlockSetInIntervalStrategy(new ArrayList<>(blocks)));
				case CONSTANT_REBUILD_IN_INTERVAL:
					return new Region(regionName, world.uniqueId(), selectionPoints.getFirstPoint(), selectionPoints.getSecondPoint(), 60, new RebuildInIntervalStrategy());
				case SAME_BLOCK:
				default:
					return new Region(regionName, world.uniqueId(), selectionPoints.getFirstPoint(), selectionPoints.getSecondPoint(), 10, new RebuildSameBlockStrategy());
			}
		}
		catch (Exception exception)
		{

			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(text("Could not create region. Reason: " + exception.getMessage())), exception);
		}
	}
}
