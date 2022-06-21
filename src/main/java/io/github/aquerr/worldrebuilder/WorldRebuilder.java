
package io.github.aquerr.worldrebuilder;

import com.google.inject.Inject;
import io.github.aquerr.worldrebuilder.commands.*;
import io.github.aquerr.worldrebuilder.commands.args.RegionArgument;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import io.github.aquerr.worldrebuilder.listener.*;
import io.github.aquerr.worldrebuilder.managers.RegionManager;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.storage.serializer.BlockExceptionListTypeSerializer;
import io.github.aquerr.worldrebuilder.storage.serializer.BlockExceptionTypeSerializer;
import io.github.aquerr.worldrebuilder.storage.serializer.WRTypeTokens;
import ninja.leaping.configurate.ConfigurationOptions;
import org.spongepowered.api.Sponge;
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

@Plugin(id = "worldrebuilder", name = "Worldrebuilder", version = WorldRebuilder.VERSION, description = "Rebuilds destroyed blocks after specified time.", authors = {"Aquerr"})
public class WorldRebuilder
{
	public static final String VERSION = "1.3.0";

	public static final Text PLUGIN_ERROR = Text.of(TextColors.RED, "[WR] ");
	public static final Text PLUGIN_PREFIX = Text.of(TextColors.GREEN, "[WR] ");

	private final Map<List<String>, CommandCallable> subcommands = new HashMap<>();
	private final Map<UUID, SelectionPoints> playerSelectionPoints = new HashMap<>();

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
		try
		{
			Sponge.getServer().getConsole().sendMessage(Text.of(PLUGIN_PREFIX, TextColors.YELLOW, "Initializing WorldRebuilder..."));
			registerTypeSerializers();
			setupManagers();
			registerCommands();
			registerListeners();
			Sponge.getServer().getConsole().sendMessage(Text.of(PLUGIN_PREFIX, TextColors.GREEN, "Loading completed. Plugin is ready to use!"));
		}
		catch (Exception exception)
		{
			Sponge.getServer().getConsole().sendMessage(Text.of(PLUGIN_ERROR, TextColors.RED, "Error during initialization of WorldRebuilder. Plugin will become disabled. Reason: " + exception.getMessage()));
			disablePlugin();
		}
	}

	private void disablePlugin()
	{
		this.commandManager.getOwnedBy(this)
				.forEach(commandManager::removeMapping);
		this.eventManager.unregisterPluginListeners(this);
	}

	private void registerTypeSerializers()
	{
		ConfigurationOptions.defaults().getSerializers()
				.register(WRTypeTokens.BLOCK_EXCEPTION, new BlockExceptionTypeSerializer())
				.register(WRTypeTokens.BLOCK_EXCEPTION_LIST, new BlockExceptionListTypeSerializer());
	}

	private void setupManagers()
	{
		this.regionManager.init();
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

	public Map<List<String>, CommandCallable> getSubcommands()
	{
		return this.subcommands;
	}

	private void registerCommands()
	{
		//Help Command
		this.subcommands.put(Collections.singletonList("help"), CommandSpec.builder()
				.description(Text.of("Shows all available commands"))
				.permission(Permissions.HELP_COMMAND)
				.executor(new HelpCommand(this))
				.arguments(GenericArguments.optional(GenericArguments.integer(Text.of("page"))))
				.build());

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
		this.subcommands.put(Collections.singletonList("create_region"), CommandSpec.builder()
				.description(Text.of("Creates a region from selected points"))
				.permission(Permissions.CREATE_REGION_COMMAND)
				.executor(new CreateRegionCommand(this))
				.arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("name"))))
				.build());

		//Delete Region Command
		this.subcommands.put(Collections.singletonList("delete_region"), CommandSpec.builder()
				.description(Text.of("Deletes a region"))
				.permission(Permissions.DELETE_COMMAND)
				.executor(new DeleteRegionCommand(this))
				.arguments(GenericArguments.onlyOne(new RegionArgument(this, Text.of("region"))))
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

		//Active Command
		final CommandSpec activeCommand = CommandSpec.builder()
				.description(Text.of("Activates/Deactivates a region"))
				.permission(Permissions.ACTIVE_COMMAND)
				.executor(new ActiveCommand(this))
				.arguments(GenericArguments.onlyOne(GenericArguments.bool(Text.of("isActive"))))
				.build();

		//DropBlocks Command
		final CommandSpec blockDropCommand = CommandSpec.builder()
				.description(Text.of("Toggles block drop in region"))
				.permission(Permissions.DROP_BLOCKS_COMMAND)
				.executor(new BlockDropCommand(this))
				.arguments(GenericArguments.onlyOne(GenericArguments.bool(Text.of("value"))))
				.build();

		//ForceRebuild Command
		final CommandSpec forceRebuildCommand = CommandSpec.builder()
				.description(Text.of("Force rebuilds region"))
				.permission(Permissions.FORCE_REBUILD_COMMAND)
				.executor(new ForceRebuildCommand(this))
				.build();

		//Region Command/s
		this.subcommands.put(Collections.singletonList("region"), CommandSpec.builder()
				.description(Text.of("Region commands"))
				.permission(Permissions.REGION_COMMANDS)
				.arguments(GenericArguments.onlyOne(new RegionArgument(this, Text.of("region"))))
				.child(regionCommand, "info")
				.child(restoreTimeCommand, "restore_time")
				.child(activeCommand, "active")
				.child(blockDropCommand, "drop_blocks")
				.child(forceRebuildCommand, "force_rebuild")
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
		this.eventManager.registerListeners(this, new EntityDestroyListener(this));
		this.eventManager.registerListeners(this, new EntitySpawnListener(this));
		this.eventManager.registerListeners(this, new BlockPlaceListener(this));
	}
}
