package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WandCommand extends WRCommand
{
	public WandCommand(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		if(!(context.cause().audience() instanceof ServerPlayer))
			throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(Component.text("Only in-game players can use this command!", NamedTextColor.RED)));

		final ServerPlayer player = (ServerPlayer) context.cause().audience();
		final Inventory inventory = player.inventory();

		final List<Component> wandDescriptionLines = new ArrayList<>();
		final Component firstLine = Component.text("Select first point with your", NamedTextColor.GOLD).append(Component.text(" left click."));
		final Component secondLine = Component.text("Select second point with your", NamedTextColor.GOLD).append(Component.text(" right click."));
		wandDescriptionLines.add(firstLine);
		wandDescriptionLines.add(secondLine);

		final ItemStack worldRebuilderWand = ItemStack.builder()
				.itemType(ItemTypes.IRON_AXE)
				.quantity(1)
				.add(Keys.CUSTOM_NAME, Component.text("WorldRebuilder Wand", NamedTextColor.GOLD))
				.add(Keys.LORE, wandDescriptionLines)
				.build();

		inventory.offer(worldRebuilderWand);
		return CommandResult.success();
	}
}
