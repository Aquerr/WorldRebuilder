
package io.github.aquerr.worldrebuilder;

import com.google.inject.Inject;
import io.github.aquerr.worldrebuilder.commands.*;
import io.github.aquerr.worldrebuilder.commands.args.RegionArgument;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import io.github.aquerr.worldrebuilder.listener.BlockBreakListener;
import io.github.aquerr.worldrebuilder.listener.WandUsageListener;
import io.github.aquerr.worldrebuilder.managers.RegionManager;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.*;

@Plugin(id = "worldrebuilder", name = "Worldrebuilder", description = "Rebuilds blocks that have been previously destroyed.", authors = {"Aquerr"})
public class WorldRebuilder
{
	public static final Text PLUGIN_ERROR = Text.of(TextColors.RED, "[WR] ");
	public static final Text PLUGIN_PREFIX = Text.of(TextColors.GREEN, "[WR] ");

	private final Map<List<String>, CommandCallable> subcommands = new HashMap<>();
	private final Map<UUID, SelectionPoints> playerSelectionPoints = new HashMap<>();

	@Inject
	private Logger logger;

	private static WorldRebuilder INSTANCE;

	private final CommandManager commandManager;
	private final EventManager eventManager;
	private final Path configDir;

	private final WorldRebuilderScheduler worldRebuilderScheduler;
	private final RegionManager regionManager;

	@Inject
	public WorldRebuilder(final CommandManager commandManager, final EventManager eventManager, final RegionManager regionManager, final WorldRebuilderScheduler worldRebuilderScheduler, final @ConfigDir(sharedRoot = false) Path configDir)
	{
		INSTANCE = this;
		this.commandManager = commandManager;
		this.eventManager = eventManager;
		this.configDir = configDir;

		this.regionManager = regionManager;
		this.worldRebuilderScheduler = worldRebuilderScheduler;
	}

	public static WorldRebuilder getPlugin()
	{
		return INSTANCE;
	}

	@Listener
	public void onInit(final GameInitializationEvent event)
	{
		registerCommands();
		registerListeners();
	}

	public Path getConfigDir()
	{
		return this.configDir;
	}

	public Map<UUID, SelectionPoints> getPlayerSelectionPoints()
	{
		return this.playerSelectionPoints;
	}

	public RegionManager getRegionManager()
	{
		return this.regionManager;
	}

	public WorldRebuilderScheduler getWorldRebuilderScheduler()
	{
		return this.worldRebuilderScheduler;
	}

	private void registerCommands()
	{

		//Wand Command
		this.subcommands.put(Collections.singletonList("wand"), CommandSpec.builder()
				.description(Text.of("Gives WorldRebuilder wand"))
				.permission(Permissions.WAND_COMMAND)
				.executor(new WandCommand(this))
				.build());

		//List Command
		this.subcommands.put(Collections.singletonList("list"), CommandSpec.builder()
				.description(Text.of("Shows a list of all regions"))
				.permission(Permissions.LIST_COMMAND)
				.executor(new ListCommand(this))
				.build());

		//Create Region Command
		this.subcommands.put(Collections.singletonList("createregion"), CommandSpec.builder()
				.description(Text.of("Creates a region from selected points"))
				.permission(Permissions.CREATE_REGION_COMMAND)
				.executor(new CreateRegionCommand(this))
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("name"))))
				.build());

		//Info Command
		final CommandSpec regionCommand = CommandSpec.builder()
				.description(Text.of("Shows information about the region"))
				.permission(Permissions.INFO_COMMAND)
				.executor(new InfoCommand(this))
				.build();

		//RestoreTime Command
		final CommandSpec restoreTimeCommand = CommandSpec.builder()
				.description(Text.of("Sets region restore time"))
				.permission(Permissions.RESTORE_TIME_COMMAND)
				.executor(new RestoreTimeCommand(this))
				.arguments(GenericArguments.onlyOne(GenericArguments.integer(Text.of("timeInSeconds"))))
				.build();

		//Region Command/s
		this.subcommands.put(Collections.singletonList("region"), CommandSpec.builder()
				.description(Text.of("Region commands"))
				.permission(Permissions.REGION_COMMANDS)
				.arguments(GenericArguments.onlyOne(new RegionArgument(this, Text.of("region"))))
				.child(regionCommand, "info")
				.child(restoreTimeCommand, "restoretime")
				.build());

		//WorldRebuilder commands
		final CommandSpec wrCommand = CommandSpec.builder()
				.children(this.subcommands)
				.build();

		this.commandManager.register(this, wrCommand, "worldrebuilder", "wr");
	}

	private void registerListeners()
	{
		this.eventManager.registerListeners(this, new WandUsageListener(this));
		this.eventManager.registerListeners(this, new BlockBreakListener(this));
	}
}
