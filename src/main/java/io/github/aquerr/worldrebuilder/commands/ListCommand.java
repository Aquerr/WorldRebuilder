package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.messaging.MessageSource;
import io.github.aquerr.worldrebuilder.model.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class ListCommand extends WRCommand
{
	private final MessageSource messageSource;

	public ListCommand(final WorldRebuilder plugin)
	{
		super(plugin);
		this.messageSource = plugin.getMessageSource();
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		final List<Component> helpList = new LinkedList<>();

		for(final Region region : regions)
		{
			final Component regionTooltipInfo = InfoCommand.createRegionInfoComponent(messageSource, region);
			final Component regionRecord = LinearComponents.linear(text(" - ", BLUE), text(region.getName(), GOLD))
					.hoverEvent(HoverEvent.showText(regionTooltipInfo));

			helpList.add(regionRecord);
		}

		final PaginationList paginationList = PaginationList.builder()
				.title(messageSource.resolveComponentWithMessage("command.region.info.header"))
				.contents(helpList)
				.padding(text("-", BLUE))
				.build();
		paginationList.sendTo(context.cause().audience());
		return CommandResult.success();
	}
}
