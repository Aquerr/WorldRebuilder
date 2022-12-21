
package io.github.aquerr.worldrebuilder;

import com.google.inject.Inject;
import io.github.aquerr.worldrebuilder.commands.ActiveCommand;
import io.github.aquerr.worldrebuilder.commands.BlockDropCommand;
import io.github.aquerr.worldrebuilder.commands.CreateRegionCommand;
import io.github.aquerr.worldrebuilder.commands.DeleteRegionCommand;
import io.github.aquerr.worldrebuilder.commands.ForceRebuildCommand;
import io.github.aquerr.worldrebuilder.commands.HelpCommand;
import io.github.aquerr.worldrebuilder.commands.InfoCommand;
import io.github.aquerr.worldrebuilder.commands.ListCommand;
import io.github.aquerr.worldrebuilder.commands.RegionCommand;
import io.github.aquerr.worldrebuilder.commands.RestoreTimeCommand;
import io.github.aquerr.worldrebuilder.commands.WandCommand;
import io.github.aquerr.worldrebuilder.commands.args.WorldRebuilderCommandParameters;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import io.github.aquerr.worldrebuilder.listener.BlockBreakListener;
import io.github.aquerr.worldrebuilder.listener.BlockPlaceListener;
import io.github.aquerr.worldrebuilder.listener.EntityDestroyListener;
import io.github.aquerr.worldrebuilder.listener.EntitySpawnListener;
import io.github.aquerr.worldrebuilder.listener.WandUsageListener;
import io.github.aquerr.worldrebuilder.managers.RegionManager;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyType;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Plugin("worldrebuilder")
public class WorldRebuilder
{
	public static final TextComponent PLUGIN_ERROR = Component.text("[WR] ", NamedTextColor.RED);
	public static final TextComponent PLUGIN_PREFIX = Component.text("[WR] ", NamedTextColor.GREEN);

	private final Map<List<String>, Command.Parameterized> subcommands = new HashMap<>();
	private final Map<UUID, SelectionPoints> playerSelectionPoints = new HashMap<>();

	private static WorldRebuilder INSTANCE;

	private final Path configDir;

	private WorldRebuilderScheduler worldRebuilderScheduler;
	private final RegionManager regionManager;

	private final PluginContainer pluginContainer;

	private final Logger logger;
	private boolean isDisabled;

	@Inject
	public WorldRebuilder(final PluginContainer pluginContainer,
						  final RegionManager regionManager,
						  final @ConfigDir(sharedRoot = false) Path configDir)
	{
		INSTANCE = this;
		this.configDir = configDir;
		this.pluginContainer = pluginContainer;
		this.logger = pluginContainer.logger();
		this.regionManager = regionManager;
	}

	public static WorldRebuilder getPlugin()
	{
		return INSTANCE;
	}

	@Listener
	public void onPluginConstruct(final ConstructPluginEvent event)
	{
		try
		{
			this.logger.info(PLUGIN_PREFIX.content() + "Initializing World Rebuilder...");
			setupManagers();
			this.logger.info(PLUGIN_PREFIX.content() + "Loading completed. Plugin is ready to use!");
		}
		catch (Exception exception)
		{
			this.logger.error(PLUGIN_ERROR.content() + "Error during initialization of WorldRebuilder. Plugin will become disabled.", exception);
			disablePlugin();
		}
	}

	@Listener
	public void onPluginLoad(final LoadedGameEvent event)
	{
		this.worldRebuilderScheduler = new WorldRebuilderScheduler(Sponge.server().scheduler());
		registerListeners();
		this.regionManager.reloadRegions();
	}

	@Listener
	public void onCommandRegister(final RegisterCommandEvent<Command.Parameterized> event)
	{
		if (this.isDisabled)
			return;

		//Register commands...
		registerCommands(event);
		this.logger.info(PLUGIN_PREFIX.content() + "Commands loaded!");
	}

	private void disablePlugin()
	{
		this.isDisabled = true;
		Sponge.eventManager().unregisterListeners(this);
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

	public Map<List<String>, Command.Parameterized> getSubcommands()
	{
		return this.subcommands;
	}



	private void registerCommands(RegisterCommandEvent<Command.Parameterized> event)
	{
		WorldRebuilderCommandParameters.init(this.regionManager);

		//Help Command
		this.subcommands.put(Collections.singletonList("help"), Command.builder()
				.shortDescription(Component.text("Shows all available commands"))
				.permission(Permissions.HELP_COMMAND)
				.executor(new HelpCommand(this))
				.addParameter(Parameter.integerNumber().key("page").optional().build())
				.build());

		//Wand Command
		this.subcommands.put(Collections.singletonList("wand"), Command.builder()
				.shortDescription(Component.text("Gives WorldRebuilder wand"))
				.permission(Permissions.WAND_COMMAND)
				.executor(new WandCommand(this))
				.build());

		//List Command
		this.subcommands.put(Collections.singletonList("list"), Command.builder()
				.shortDescription(Component.text("Shows a list of all regions"))
				.permission(Permissions.LIST_COMMAND)
				.executor(new ListCommand(this))
				.build());

		//Create Region Command
		this.subcommands.put(Collections.singletonList("create_region"), Command.builder()
				.shortDescription(Component.text("Creates a region from selected points"))
				.permission(Permissions.CREATE_REGION_COMMAND)
				.executor(new CreateRegionCommand(this))
				.addParameter(Parameter.string().key("name").build())
				.addParameter(Parameter.enumValue(RebuildStrategyType.class).key("strategyType").optional().build())
				.addParameter(Parameter.blockState().key("blockList").optional().consumeAllRemaining().build())
				.build());

		//Delete Region Command
		this.subcommands.put(Collections.singletonList("delete_region"), Command.builder()
				.shortDescription(Component.text("Deletes a region"))
				.permission(Permissions.DELETE_COMMAND)
				.executor(new DeleteRegionCommand(this))
				.addParameter(WorldRebuilderCommandParameters.region())
				.build());

		//Info Command
		final Command.Parameterized regionCommand = Command.builder()
				.shortDescription(Component.text("Shows information about the region"))
				.permission(Permissions.INFO_COMMAND)
				.executor(new InfoCommand(this))
				.build();

		//RestoreTime Command
		final Command.Parameterized restoreTimeCommand = Command.builder()
				.shortDescription(Component.text("Sets region restore time"))
				.permission(Permissions.RESTORE_TIME_COMMAND)
				.executor(new RestoreTimeCommand(this))
				.addParameter(Parameter.integerNumber().key("timeInSeconds").build())
				.build();

		//Active Command
		final Command.Parameterized activeCommand = Command.builder()
				.shortDescription(Component.text("Activates/Deactivates a region"))
				.permission(Permissions.ACTIVE_COMMAND)
				.executor(new ActiveCommand(this))
				.addParameter(Parameter.bool().key("isActive").build())
				.build();

		//DropBlocks Command
		final Command.Parameterized blockDropCommand = Command.builder()
				.shortDescription(Component.text("Toggles block drop in region"))
				.permission(Permissions.DROP_BLOCKS_COMMAND)
				.executor(new BlockDropCommand(this))
				.addParameter(Parameter.bool().key("value").build())
				.build();

		//ForceRebuild Command
		final Command.Parameterized forceRebuildCommand = Command.builder()
				.shortDescription(Component.text("Force rebuilds region"))
				.permission(Permissions.FORCE_REBUILD_COMMAND)
				.executor(new ForceRebuildCommand(this))
				.build();

		//Region Command/s
		this.subcommands.put(Collections.singletonList("region"), Command.builder()
				.shortDescription(Component.text("Region commands"))
				.permission(Permissions.REGION_COMMANDS)
				.executor(new RegionCommand(this))
				.addParameters(
						WorldRebuilderCommandParameters.region(),
						Parameter.firstOf(
							Parameter.subcommand(regionCommand, "info"),
							Parameter.subcommand(restoreTimeCommand, "restore_time"),
							Parameter.subcommand(activeCommand, "active"),
							Parameter.subcommand(blockDropCommand, "drop_blocks"),
							Parameter.subcommand(forceRebuildCommand, "force_rebuild")
						)
				)
				.build());

		//WorldRebuilder commands
		final Command.Parameterized wrCommand = Command.builder()
				.executor(new HelpCommand(this))
				.addChildren(this.subcommands)
				.build();

		//Register commands
		event.register(this.pluginContainer, wrCommand, "worldrebuilder", "wr");
	}

	private void registerListeners()
	{
		EventManager eventManager = Sponge.eventManager();
		eventManager.registerListeners(this.pluginContainer, new WandUsageListener(this));
		eventManager.registerListeners(this.pluginContainer, new BlockBreakListener(this));
		eventManager.registerListeners(this.pluginContainer, new EntityDestroyListener(this));
		eventManager.registerListeners(this.pluginContainer, new EntitySpawnListener(this));
		eventManager.registerListeners(this.pluginContainer, new BlockPlaceListener(this));
	}

	public PluginContainer getPluginContainer()
	{
		return this.pluginContainer;
	}
}
