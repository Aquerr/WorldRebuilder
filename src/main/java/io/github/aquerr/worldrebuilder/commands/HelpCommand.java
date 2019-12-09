package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HelpCommand extends WRCommand
{
	public HelpCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
	{
		final Optional<Integer> helpPage = args.getOne(Text.of("page"));
		int pageNumber = helpPage.orElse(1);
		final Map<List<String>, CommandCallable> subcommands = super.getPlugin().getSubcommands();
		final List<Text> helpList = new ArrayList<>();

		for (final List<String> aliases: subcommands.keySet())
		{
			final CommandCallable commandCallable = subcommands.get(aliases);
			if(source instanceof Player)
			{
				final Player player = (Player)source;
				if(!commandCallable.testPermission(player))
				{
					continue;
				}
			}

			final Text.Builder textBuilder = Text.builder();
			textBuilder.append(Text.of(TextColors.GOLD, "/wr " + String.join(", ", aliases) + " " + commandCallable.getUsage(source).toPlain()));
			textBuilder.append(Text.of(TextColors.WHITE, " - " + commandCallable.getShortDescription(source).get().toPlain()));
			helpList.add(textBuilder.build());
		}

		helpList.sort(Text::compareTo);

		final PaginationList paginationList = PaginationList.builder().linesPerPage(16).padding(Text.of(TextColors.BLUE, "-")).title(Text.of(TextColors.GOLD, "Commands List")).contents(helpList).build();
		paginationList.sendTo(source, pageNumber);

		return CommandResult.success();
	}
}
