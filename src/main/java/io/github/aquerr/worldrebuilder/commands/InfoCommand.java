package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
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
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class InfoCommand extends WRCommand
{
	public InfoCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		final Region region = context.requireOne(Parameter.key("region", Region.class));
		final List<Component> helpList = new LinkedList<>();

		String worldName = "Not Found";
		final Optional<ServerWorld> optionalWorld = WorldUtils.getWorldByUUID(region.getWorldUniqueId());
		if(optionalWorld.isPresent())
			worldName = optionalWorld.get().properties().name();


		final Component component = LinearComponents.linear(
				text(" - Name: ", BLUE), text(region.getName(), GOLD), newline(),
				text(" - World Name: ", BLUE), text(worldName, GOLD), newline(),
				text(" - First Point: ", BLUE), text(region.getFirstPoint().toString(), GOLD), newline(),
				text(" - Second Point: ", BLUE), text(region.getSecondPoint().toString(), GOLD), newline(),
				text(" - Restore Time: ", BLUE), text(region.getRestoreTime(), GOLD), newline(),
				text(" - Drops blocks: ", BLUE), text(region.shouldDropBlocks(), GOLD), newline(),
				text(" - Active: ", BLUE), text(region.isActive(), region.isActive() ? GREEN : RED)
		);

		helpList.add(component);
		final PaginationList paginationList = PaginationList.builder()
				.title(text("Region Info", GOLD))
				.contents(helpList).linesPerPage(14)
				.padding(text("-", BLUE))
				.build();
		paginationList.sendTo(context.cause().audience());
		return CommandResult.success();
	}
}
