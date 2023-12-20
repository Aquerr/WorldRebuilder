package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.messaging.MessageSource;
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
	private final MessageSource messageSource;

	public WandCommand(final WorldRebuilder plugin)
	{
		super(plugin);
		this.messageSource = plugin.getMessageSource();
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException
	{
		if(!(context.cause().audience() instanceof ServerPlayer))
			throw messageSource.resolveExceptionWithMessage("error.command.in-game-player-required");

		final ServerPlayer player = (ServerPlayer) context.cause().audience();
		final Inventory inventory = player.inventory();

		final List<Component> wandDescriptionLines = new ArrayList<>();
		final Component firstLine = messageSource.resolveComponentWithMessage("command.region.wand.select-first-point");
		final Component secondLine = messageSource.resolveComponentWithMessage("command.region.wand-select-second-point");
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
