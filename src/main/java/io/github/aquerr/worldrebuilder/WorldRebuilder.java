
package io.github.aquerr.worldrebuilder;

import com.google.inject.Inject;
import io.github.aquerr.worldrebuilder.commands.CreateRegionCommand;
import io.github.aquerr.worldrebuilder.commands.WandCommand;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import io.github.aquerr.worldrebuilder.listener.BlockBreakListener;
import io.github.aquerr.worldrebuilder.listener.WandUsageListener;
import io.github.aquerr.worldrebuilder.managers.RegionManager;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.nio.file.Path;
import java.util.*;

@Plugin(id = "worldrebuilder", name = "Worldrebuilder", description = "Rebuilds blocks that have been previously destroyed.", authors = {"Aquerr"})
public class WorldRebuilder
{
	public static final Text PLUGIN_ERROR = Text.of(TextColors.RED, "[WR]" );
	public static final Text PLUGIN_PREFIX = Text.of(TextColors.GREEN, "[WR]" );

	private final Map<List<String>, CommandCallable> subcommands = new HashMap<>();
	private final Map<UUID, SelectionPoints> playerSelectionPoints = new HashMap<>();

	@Inject
	private Logger logger;

	private final CommandManager commandManager;
	private final EventManager eventManager;
	private final Path configDir;

	private final RegionManager regionManager;

	@Inject
	public WorldRebuilder(final CommandManager commandManager, final EventManager eventManager, final RegionManager regionManager, final @ConfigDir(sharedRoot = false) Path configDir)
	{
		this.commandManager = commandManager;
		this.eventManager = eventManager;
		this.configDir = configDir;

		this.regionManager = regionManager;
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

	private void registerCommands()
	{
		//Wand Command
		this.subcommands.put(Collections.singletonList("wand"), CommandSpec.builder()
				.description(Text.of("Gives WorldRebuilder wand"))
				.permission(Permissions.WAND_COMMAND)
				.executor(new WandCommand(this))
				.build());

		//Create Region Command
		this.subcommands.put(Collections.singletonList("createregion"), CommandSpec.builder()
				.description(Text.of("Creates a region from selected points"))
				.permission(Permissions.CREATE_REGION_COMMAND)
				.executor(new CreateRegionCommand(this))
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
