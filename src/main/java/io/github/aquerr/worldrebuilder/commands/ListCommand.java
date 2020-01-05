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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ListCommand extends WRCommand
{
	public ListCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
	{
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		final List<Text> helpList = new LinkedList<>();

		for(final Region region : regions)
		{
			String worldName = "Not Found";
			final Optional<World> optionalWorld = Sponge.getServer().getWorld(region.getWorldUniqueId());
			if(optionalWorld.isPresent())
				worldName = optionalWorld.get().getName();

			final Text regionTooltipInfo = Text.builder()
					.append(Text.of(TextColors.BLUE,  "Name: ", TextColors.GOLD, region.getName(), "\n"))
					.append(Text.of(TextColors.BLUE, "World Name: ", TextColors.GOLD, worldName, "\n"))
					.append(Text.of(TextColors.BLUE, "First Point: ", TextColors.GOLD, region.getFirstPoint(), "\n"))
					.append(Text.of(TextColors.BLUE, "Second Point: ", TextColors.GOLD, region.getSecondPoint(), "\n"))
					.append(Text.of(TextColors.BLUE, "Restore Time: ", (region.isActive() ? TextColors.GREEN : TextColors.RED), region.getRestoreTime(), "\n"))
					.append(Text.of(TextColors.BLUE, "Active: ", TextColors.GOLD, region.isActive()))
					.build();

			final Text regionRecord = Text.builder()
					.append(Text.of(TextActions.showText(regionTooltipInfo), TextColors.BLUE, " - ", TextColors.GOLD, region.getName()))
					.build();

			helpList.add(regionRecord);
		}

		final PaginationList paginationList = PaginationList.builder().title(Text.of(TextColors.GOLD, "Regions List")).contents(helpList).padding(Text.of(TextColors.BLUE, "-")).build();
		paginationList.sendTo(source);
		return CommandResult.success();
	}
}
