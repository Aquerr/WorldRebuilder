package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class ListCommand extends WRCommand
{
	public ListCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		final List<Component> helpList = new LinkedList<>();

		for(final Region region : regions)
		{
			String worldName = "Not Found";
			final Optional<ServerWorld> optionalWorld = WorldUtils.getWorldByUUID(region.getWorldUniqueId());
			if(optionalWorld.isPresent())
				worldName = optionalWorld.get().properties().name();

			final Component regionTooltipInfo = LinearComponents.linear(
					text("Name: ", BLUE), text(region.getName(), GOLD), newline(),
					text("World Name: ", BLUE), text(worldName), newline(),
					text("First Point: ", BLUE), text(region.getFirstPoint().toString()), newline(),
					text("Second Point: ", BLUE), text(region.getSecondPoint().toString()), newline(),
					text("Restore Time: ", BLUE), text(region.getRestoreTime(), region.isActive() ? GREEN : RED), newline(),
					text("Rebuild Strategy: ", BLUE), text(region.getRebuildBlocksStrategy().getType().toString()), newline(),
					text("Active: ", BLUE), text(region.isActive(), region.isActive() ? GREEN : RED)
			);

			final Component regionRecord = LinearComponents.linear(text(" - ", BLUE), text(region.getName(), GOLD))
					.hoverEvent(HoverEvent.showText(regionTooltipInfo));

			helpList.add(regionRecord);
		}

		final PaginationList paginationList = PaginationList.builder()
				.title(text("Regions List", GOLD))
				.contents(helpList)
				.padding(text("-", BLUE))
				.build();
		paginationList.sendTo(context.cause().audience());
		return CommandResult.success();	}
}
