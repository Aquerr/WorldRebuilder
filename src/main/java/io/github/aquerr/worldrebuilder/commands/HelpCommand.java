package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class HelpCommand extends WRCommand
{
	public HelpCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		final Optional<Integer> helpPage = context.one(Parameter.integerNumber().key("page").build());
		int pageNumber = helpPage.orElse(1);
		final Map<List<String>, Command.Parameterized> subcommands = super.getPlugin().getSubcommands();
		final List<Component> helpList = new ArrayList<>();

		for (final Map.Entry<List<String>, Command.Parameterized> command : subcommands.entrySet())
		{
			if(context.cause().audience() instanceof Player && !command.getValue().canExecute(context.cause()))
				continue;

			final Component commandHelp = LinearComponents.linear(
					text("/wr " + String.join(", ", command.getKey()), GOLD),
					text(" - ").append(command.getValue().shortDescription(CommandCause.create()).get().color(WHITE)),
			        Component.newline()
			);
			helpList.add(commandHelp);
		}

		helpList.sort(Comparator.comparing(o -> PlainTextComponentSerializer.plainText().serialize(o)));

		final PaginationList paginationList = PaginationList.builder()
				.linesPerPage(16)
				.padding(text("-", NamedTextColor.BLUE))
				.title(getPlugin().getMessageSource().resolveComponentWithMessage("command.help.command-list"))
				.contents(helpList).build();
		paginationList.sendTo(context.cause().audience(), pageNumber);

		return CommandResult.success();
	}
}
