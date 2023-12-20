
package io.github.aquerr.worldrebuilder;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.aquerr.worldrebuilder.commands.ActiveCommand;
import io.github.aquerr.worldrebuilder.commands.AddNotificationCommand;
import io.github.aquerr.worldrebuilder.commands.BlockDropCommand;
import io.github.aquerr.worldrebuilder.commands.CreateRegionCommand;
import io.github.aquerr.worldrebuilder.commands.DeleteNotificationCommand;
import io.github.aquerr.worldrebuilder.commands.DeleteRegionCommand;
import io.github.aquerr.worldrebuilder.commands.ForceRebuildCommand;
import io.github.aquerr.worldrebuilder.commands.HelpCommand;
import io.github.aquerr.worldrebuilder.commands.InfoCommand;
import io.github.aquerr.worldrebuilder.commands.ListCommand;
import io.github.aquerr.worldrebuilder.commands.ListNotificationsCommand;
import io.github.aquerr.worldrebuilder.commands.RegionCommand;
import io.github.aquerr.worldrebuilder.commands.RestoreTimeCommand;
import io.github.aquerr.worldrebuilder.commands.SchedulerTasksCommand;
import io.github.aquerr.worldrebuilder.commands.StrategyCommand;
import io.github.aquerr.worldrebuilder.commands.WandCommand;
import io.github.aquerr.worldrebuilder.commands.args.WorldRebuilderCommandParameters;
import io.github.aquerr.worldrebuilder.config.Configuration;
import io.github.aquerr.worldrebuilder.config.ConfigurationImpl;
import io.github.aquerr.worldrebuilder.messaging.MessageSource;
import io.github.aquerr.worldrebuilder.messaging.WRMessageSource;
import io.github.aquerr.worldrebuilder.model.SelectionPoints;
import io.github.aquerr.worldrebuilder.listener.BlockBreakListener;
import io.github.aquerr.worldrebuilder.listener.BlockPlaceListener;
import io.github.aquerr.worldrebuilder.listener.EntityDestroyListener;
import io.github.aquerr.worldrebuilder.listener.EntitySpawnListener;
import io.github.aquerr.worldrebuilder.listener.WandUsageListener;
import io.github.aquerr.worldrebuilder.managers.RegionManager;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyType;
import io.github.aquerr.worldrebuilder.util.resource.Resource;
import io.github.aquerr.worldrebuilder.util.resource.ResourceUtils;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static net.kyori.adventure.text.Component.text;

@Plugin("worldrebuilder")
public class WorldRebuilder
{
	public static final String PLUGIN_PREFIX_PLAIN = "[WR] ";
	public static final TextComponent PLUGIN_ERROR = text(PLUGIN_PREFIX_PLAIN, NamedTextColor.RED);
	public static final TextComponent PLUGIN_PREFIX = text(PLUGIN_PREFIX_PLAIN, NamedTextColor.GREEN);

	private final Map<List<String>, Command.Parameterized> subcommands = new HashMap<>();
	private final Map<UUID, SelectionPoints> playerSelectionPoints = new HashMap<>();

	private static WorldRebuilder INSTANCE;

	private final Path configDir;

	private Configuration configuration;

	private WorldRebuilderScheduler worldRebuilderScheduler;
	private final RegionManager regionManager;
	private MessageSource messageSource;

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
			setupConfigs();
			setupLocalization();
			this.logger.info(messageSource.resolveMessage("plugin.initializing", PLUGIN_PREFIX_PLAIN));
			setupManagers();
			this.logger.info(messageSource.resolveMessage("plugin.loading-completed", PLUGIN_PREFIX_PLAIN));
		}
		catch (Exception exception)
		{
			this.logger.error(messageSource.resolveMessage("plugin.initialization-error", PLUGIN_PREFIX_PLAIN), exception);
			disablePlugin();
		}
	}

	@Listener
	public void onPluginLoad(final LoadedGameEvent event)
	{
		if (this.isDisabled)
			return;

		this.worldRebuilderScheduler = new WorldRebuilderScheduler(Sponge.server().scheduler(), Sponge.game().asyncScheduler(), this.logger);
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
		this.logger.info(messageSource.resolveMessage("plugin.commands-loaded", PLUGIN_PREFIX_PLAIN));
	}

	private void disablePlugin()
	{
		this.isDisabled = true;
		Sponge.eventManager().unregisterListeners(this);
	}

	private void setupConfigs() throws IOException
	{
		Resource resource = ResourceUtils.getResource("assets/worldrebuilder/" + ConfigurationImpl.CONFIG_FILE_NAME);
		if (resource == null)
			return;

		this.configuration = new ConfigurationImpl(configDir, resource);
	}

	private void setupLocalization()
	{
		WRMessageSource.init(this.configDir.resolve("messages"), this.configuration.getLangConfig().getLanguageTag());
		this.messageSource = WRMessageSource.getInstance();
	}

	private void setupManagers()
	{
		this.regionManager.init();
	}

	public Logger getLogger()
	{
		return logger;
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

	public MessageSource getMessageSource()
	{
		return messageSource;
	}

	public Configuration getConfiguration()
	{
		return configuration;
	}

	public Map<List<String>, Command.Parameterized> getSubcommands()
	{
		return this.subcommands;
	}

	private void registerCommands(RegisterCommandEvent<Command.Parameterized> event)
	{
		WorldRebuilderCommandParameters.init(this.regionManager);

		registerCommand(singletonList("help"), "command.help.desc", Permissions.HELP_COMMAND, new HelpCommand(this), Parameter.integerNumber().key("page").optional().build());
		registerCommand(singletonList("wand"), "command.wand.desc", Permissions.WAND_COMMAND, new WandCommand(this));
		registerCommand(singletonList("list"), "command.list.desc", Permissions.LIST_COMMAND, new ListCommand(this));
		registerCommand(singletonList("create_region"), "command.region.create.desc", Permissions.CREATE_REGION_COMMAND, new CreateRegionCommand(this),
				Parameter.string().key("name").build(),
				Parameter.enumValue(RebuildStrategyType.class).key("strategyType").build(),
				Parameter.blockState().key("blockList").optional().consumeAllRemaining().build());
		registerCommand(singletonList("delete_region"), "command.region.delete.desc", Permissions.DELETE_REGION_COMMAND, new DeleteRegionCommand(this),
				WorldRebuilderCommandParameters.region());

		//Info Command
		final Command.Parameterized infoCommand = prepareCommand("command.region.info.desc", Permissions.REGION_INFO_COMMAND, new InfoCommand(this));

		//RestoreTime Command
		final Command.Parameterized restoreTimeCommand = prepareCommand("command.region.restore_time.desc",
				Permissions.REGION_RESTORE_TIME_COMMAND,
				new RestoreTimeCommand(this),
				Parameter.integerNumber().key("timeInSeconds").build());

		//Active Command
		final Command.Parameterized activeCommand = prepareCommand("command.region.active.desc",
				Permissions.REGION_ACTIVE_COMMAND,
				new ActiveCommand(this),
				Parameter.bool().key("isActive").build());

		//BlockDrop Command
		final Command.Parameterized blockDropCommand = prepareCommand("command.region.block_drop.desc",
				Permissions.REGION_DROP_BLOCKS_COMMAND,
				new BlockDropCommand(this),
				Parameter.bool().key("value").build());

		//ForceRebuild Command
		final Command.Parameterized forceRebuildCommand = prepareCommand("command.region.force_rebuild.desc",
				Permissions.REGION_FORCE_REBUILD_COMMAND,
				new ForceRebuildCommand(this));

		//Change strategy Command
		final Command.Parameterized strategyCommand = prepareCommand("command.region.strategy.desc",
				Permissions.REGION_STRATEGY_COMMAND,
				new StrategyCommand(this),
				Parameter.enumValue(RebuildStrategyType.class).key("strategyType").build(),
				Parameter.blockState().key("blockList").optional().consumeAllRemaining().build());

		// List notifications command
		final Command.Parameterized listNotificationsCommand = prepareCommand("command.region.list_notifications.desc",
				Permissions.REGION_LIST_NOTIFICATIONS_COMMAND,
				new ListNotificationsCommand(this));

		// Add notification command
		final Command.Parameterized addNotificationCommand = prepareCommand("command.region.notification.add.desc",
				Permissions.REGION_ADD_NOTIFICATION_COMMAND,
				new AddNotificationCommand(this),
				Parameter.duration().key("timeBeforeRebuild").build(),
				Parameter.formattingCodeTextOfRemainingElements().key("message").build());

		// Delete notification command
		final Command.Parameterized deleteNotificationCommand = prepareCommand("command.region.notification.delete.desc",
				Permissions.REGION_DELETE_NOTIFICATION_COMMAND,
				new DeleteNotificationCommand(this),
				Parameter.duration().key("timeBeforeRebuild").build());

		registerCommand(singletonList("region"), "command.region.desc", Permissions.REGION_COMMANDS, new RegionCommand(this),
				WorldRebuilderCommandParameters.region(),
				Parameter.firstOf(
						Parameter.subcommand(infoCommand, "info"),
						Parameter.subcommand(restoreTimeCommand, "restore_time"),
						Parameter.subcommand(activeCommand, "active"),
						Parameter.subcommand(blockDropCommand, "drop_blocks"),
						Parameter.subcommand(forceRebuildCommand, "force_rebuild"),
						Parameter.subcommand(strategyCommand, "strategy"),
						Parameter.subcommand(listNotificationsCommand, "list_notifications"),
						Parameter.subcommand(addNotificationCommand, "add_notification"),
						Parameter.subcommand(deleteNotificationCommand, "delete_notification")
				));

		registerCommand(singletonList("tasks"), "command.tasks.desc", Permissions.SCHEDULER_TASKS_COMMAND, new SchedulerTasksCommand(this));

		//WorldRebuilder commands
		final Command.Parameterized wrCommand = Command.builder()
				.executor(new HelpCommand(this))
				.addChildren(this.subcommands)
				.build();

		//Register commands
		event.register(this.pluginContainer, wrCommand, "worldrebuilder", "wr");
	}

	private Command.Parameterized prepareCommand(String descriptionKey, String permission, CommandExecutor commandExecutor, Parameter... parameters)
	{
		return Command.builder()
				.shortDescription(messageSource.resolveComponentWithMessage(descriptionKey))
				.permission(permission)
				.executor(commandExecutor)
				.addParameters(parameters)
				.build();
	}

	private void registerCommand(List<String> aliases, String descriptionKey, String permission, CommandExecutor commandExecutor, Parameter... parameters)
	{
		this.subcommands.put(aliases, prepareCommand(descriptionKey, permission, commandExecutor, parameters));
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
