package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class InfoCommand extends WRCommand
{
	public InfoCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		final Region region = args.requireOne(Text.of("region"));
		final List<Text> helpList = new LinkedList<>();

		String worldName = "Not Found";
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(region.getWorldUniqueId());
		if(optionalWorld.isPresent())
			worldName = optionalWorld.get().getName();


		final Text.Builder textBuilder = Text.builder();
		textBuilder.append(Text.of(TextColors.BLUE, "Name: " + TextColors.GOLD, region.getName() + "\n"))
				.append(Text.of(TextColors.BLUE, "World Name: ", TextColors.GOLD, worldName))
				.append(Text.of(TextColors.BLUE, "First Point: ", TextColors.GOLD, region.getFirstPoint()))
				.append(Text.of(TextColors.BLUE, "Second Point: ", TextColors.GOLD, region.getSecondPoint()))
				.append(Text.of(TextColors.BLUE, "Restore Time: ", TextColors.GOLD, region.getRestoreTime()));

		helpList.add(textBuilder.build());
		final PaginationList paginationList = PaginationList.builder().title(Text.of(TextColors.GOLD, "Region Info")).contents(helpList).linesPerPage(14).padding(Text.of(TextColors.BLUE, "-")).build();
		paginationList.sendTo(src);
		return CommandResult.success();
	}
}
