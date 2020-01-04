package io.github.aquerr.worldrebuilder.commands.args;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;

public class RegionArgument extends CommandElement
{
	private final WorldRebuilder plugin;

	public RegionArgument(final WorldRebuilder plugin, final @Nullable Text key)
	{
		super(key);
		this.plugin = plugin;
	}

	@Nullable
	@Override
	protected Region parseValue(final CommandSource source, final CommandArgs args) throws ArgumentParseException
	{
		final Optional<String> optionalArg = args.nextIfPresent();
		if(optionalArg.isPresent())
		{
			final String arg = optionalArg.get(); //Should be the name of the region
			return this.plugin.getRegionManager().getRegion(arg);
		}
		return null;
	}

	@Override
	public List<String> complete(final CommandSource src, final CommandArgs args, final CommandContext context)
	{
		if(args.getAll().size() > 2)
			return Collections.emptyList();

		final List<String> regionsList = new LinkedList<>();
		final Optional<String> optionalArg = args.nextIfPresent();
		if(optionalArg.isPresent())
		{
			final String arg = optionalArg.get();
			final Collection<Region> regions = this.plugin.getRegionManager().getRegions();
			for(final Region region : regions)
			{
				if(region.getName().contains(arg))
					regionsList.add(region.getName());
			}
		}
		return regionsList;
	}
}
