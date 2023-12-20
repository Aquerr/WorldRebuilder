package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.messaging.MessageSource;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.model.SelectionPoints;
import io.github.aquerr.worldrebuilder.strategy.RebuildBlocksStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyFactory;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyType;
import io.github.aquerr.worldrebuilder.strategy.WRBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateRegionCommand extends WRCommand
{
	private final MessageSource messageSource;

	public CreateRegionCommand(final WorldRebuilder plugin)
	{
		super(plugin);
		this.messageSource = plugin.getMessageSource();
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		final String name = context.requireOne(Parameter.string().key("name").build());
		final RebuildStrategyType strategyType = context.requireOne(Parameter.enumValue(RebuildStrategyType.class).key("strategyType").build());

		final Collection<? extends BlockState> blockList = context.all(Parameter.blockState().key("blockList").build());

		if (!(context.cause().audience() instanceof ServerPlayer))
			throw messageSource.resolveExceptionWithMessage("error.command.in-game-player-required");

		final ServerPlayer player = (ServerPlayer) context.cause().audience();
		if (!super.getPlugin().getPlayerSelectionPoints().containsKey(player.uniqueId()))
			throw messageSource.resolveExceptionWithMessage("command.region.create.error.you-must-select-two-points");

		final ServerWorld world = player.world();
		final SelectionPoints selectionPoints = super.getPlugin().getPlayerSelectionPoints().get(player.uniqueId());

		if (selectionPoints.getFirstPoint() == null || selectionPoints.getSecondPoint() == null)
			throw messageSource.resolveExceptionWithMessage("command.region.create.error.you-must-select-two-points");

		if (super.getPlugin().getRegionManager().getRegion(name) != null)
			throw messageSource.resolveExceptionWithMessage("command.region.create.error.region-with-such-name-already-exists");

		Region region = createRegionForSelectedStrategy(name, strategyType, blockList, world, selectionPoints);
		super.getPlugin().getRegionManager().addRegion(region);
		player.sendMessage(messageSource.resolveMessageWithPrefix("command.region.create.region-created"));
		return CommandResult.success();
	}

	private Region createRegionForSelectedStrategy(String regionName, RebuildStrategyType rebuildStrategyType, Collection<? extends BlockState> blocks, ServerWorld world, SelectionPoints selectionPoints) throws CommandException
	{
		if (rebuildStrategyType.hasPredefinedBlockSet() && blocks == null)
		{
			throw messageSource.resolveExceptionWithMessage("command.region.create.error.selected-rebuild-strategy-requires-predefined-block-set");
		}

		try
		{
			RebuildBlocksStrategy rebuildBlocksStrategy = RebuildStrategyFactory.getStrategy(rebuildStrategyType, blocks.stream().map(WRBlockState::of).collect(Collectors.toList()));
			int restoreTime = rebuildBlocksStrategy.doesRunContinuously() ? 60 : 10;

			return Region.builder()
					.name(regionName)
					.worldUniqueId(world.uniqueId())
					.firstPoint(selectionPoints.getFirstPoint())
					.secondPoint(selectionPoints.getSecondPoint())
					.restoreTime(restoreTime)
					.rebuildBlocksStrategy(rebuildBlocksStrategy)
					.notifications(prepareDefaultNotifications())
					.build();
		}
		catch (Exception exception)
		{
			throw messageSource.resolveExceptionWithMessageAndThrowable("command.region.create.error.could-not-create-region", exception);
		}
	}

	private Map<Long, String> prepareDefaultNotifications()
	{
		Map<Long, String> defaultNotifications = new HashMap<>();
		defaultNotifications.put(10L, messageSource.resolveMessage("region.default-notification.rebuild-10-seconds"));
		defaultNotifications.put(60L, messageSource.resolveMessage("region.default-notification.rebuild-1-minute"));
		return defaultNotifications;
	}
}
