package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.messaging.MessageSource;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;

public class InfoCommand extends WRCommand
{
	private final MessageSource messageSource;

	public InfoCommand(final WorldRebuilder plugin)
	{
		super(plugin);
		this.messageSource = plugin.getMessageSource();
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		final Region region = context.requireOne(Parameter.key("region", Region.class));
		final List<Component> helpList = new LinkedList<>();

		final Component component = createRegionInfoComponent(messageSource, region);

		helpList.add(component);
		final PaginationList paginationList = PaginationList.builder()
				.title(messageSource.resolveComponentWithMessage("command.region.info.header"))
				.contents(helpList).linesPerPage(14)
				.padding(text("-", BLUE))
				.build();
		paginationList.sendTo(context.cause().audience());
		return CommandResult.success();
	}

	public static Component createRegionInfoComponent(MessageSource messageSource, Region region)
	{
		String worldName = messageSource.resolveMessage("command.region.info.world-name.not-found");
		final Optional<ServerWorld> optionalWorld = WorldUtils.getWorldByUUID(region.getWorldUniqueId());
		if(optionalWorld.isPresent())
			worldName = optionalWorld.get().properties().name();

		return LinearComponents.linear(
				messageSource.resolveComponentWithMessage("command.region.info.name", region.getName()), newline(),
				messageSource.resolveComponentWithMessage("command.region.info.world-name", worldName), newline(),
				messageSource.resolveComponentWithMessage("command.region.info.first-point", region.getFirstPoint().toString()), newline(),
				messageSource.resolveComponentWithMessage("command.region.info.second-point", region.getSecondPoint().toString()), newline(),
				messageSource.resolveComponentWithMessage("command.region.info.restore-time", region.getRestoreTime()), newline(),
				region.shouldDropBlocks() ? messageSource.resolveComponentWithMessage("command.region.info.drop-blocks.yes") : messageSource.resolveComponentWithMessage("command.region.info.drop-blocks.no"), newline(),
				region.isActive() ? messageSource.resolveComponentWithMessage("command.region.info.active.yes") : messageSource.resolveComponentWithMessage("command.region.info.active.no")
		);
	}
}
