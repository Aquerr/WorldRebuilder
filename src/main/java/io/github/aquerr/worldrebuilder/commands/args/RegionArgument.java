package io.github.aquerr.worldrebuilder.commands.args;

import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.managers.RegionManager;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.configurate.util.Strings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RegionArgument
{
	public static class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<Region>
	{
		private final RegionManager regionManager;

		public ValueParser(RegionManager regionManager)
		{
			this.regionManager = regionManager;
		}

		@Override
		public Optional<Region> parseValue(Parameter.Key<? super Region> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
		{
			if (!reader.canRead())
				throw reader.createException(Component.text("Argument is not a valid region!"));

			final String regionName = reader.parseUnquotedString();
			if (Strings.isBlank(regionName))
				throw reader.createException(Component.text("Argument is not a valid region!"));
			Region region = Optional.ofNullable(this.regionManager.getRegion(regionName))
					.orElseThrow(() -> reader.createException(Component.text("Argument is not a valid region!")));
			return Optional.ofNullable(region);
		}
	}

	public static class Completer implements ValueCompleter
	{
		private final RegionManager regionManager;

		public Completer(RegionManager regionManager)
		{
			this.regionManager = regionManager;
		}

		@Override
		public List<CommandCompletion> complete(CommandContext context, String currentInput)
		{
			return this.regionManager.getRegions().stream()
					.map(Region::getName)
					.filter(regionName -> regionName.toLowerCase()
							.contains(currentInput.toLowerCase()))
					.map(CommandCompletion::of)
					.collect(Collectors.toList());
		}
	}
}
