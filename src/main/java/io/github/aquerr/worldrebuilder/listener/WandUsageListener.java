package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;

public class WandUsageListener extends AbstractListener
{
    public WandUsageListener(final WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Listener
    public void onRightClick(final InteractBlockEvent.Secondary event, final @Root Player player)
    {
        if(event.block() == BlockSnapshot.empty())
            return;

        if(player.itemInHand(HandTypes.MAIN_HAND).isEmpty())
            return;

        final ItemStack itemInHand = player.itemInHand(HandTypes.MAIN_HAND);

        if(!itemInHand.get(Keys.CUSTOM_NAME).isPresent() || !PlainTextComponentSerializer.plainText().serialize(itemInHand.get(Keys.CUSTOM_NAME).get())
                .equals(PlainTextComponentSerializer.plainText().serialize(Component.text("WorldRebuilder Wand"))))
            return;

        SelectionPoints selectionPoints = super.getPlugin().getPlayerSelectionPoints().get(player.uniqueId());
        if (selectionPoints == null)
        {
            selectionPoints = new SelectionPoints(null, event.block().position());
        }
        else
        {
            selectionPoints.setSecondPoint(event.block().position());
        }

        super.getPlugin().getPlayerSelectionPoints().put(player.uniqueId(), selectionPoints);
        player.sendMessage(LinearComponents.linear(Component.text("Second point", NamedTextColor.GOLD),
                Component.text(" has been selected at ", NamedTextColor.BLUE),
                Component.text(event.block().position().toString(), NamedTextColor.GOLD)));
        event.setCancelled(true);
    }

    @Listener
    public void onLeftClick(final InteractBlockEvent.Primary.Start event, final @Root Player player)
    {
        if(event.block() == BlockSnapshot.empty())
            return;

        if(player.itemInHand(HandTypes.MAIN_HAND).isEmpty())
            return;

        final ItemStack itemInHand = player.itemInHand(HandTypes.MAIN_HAND);

        if(!itemInHand.get(Keys.CUSTOM_NAME).isPresent() || !PlainTextComponentSerializer.plainText().serialize(itemInHand.get(Keys.CUSTOM_NAME).get())
                .equals(PlainTextComponentSerializer.plainText().serialize(Component.text("WorldRebuilder Wand"))))
            return;

        SelectionPoints selectionPoints = super.getPlugin().getPlayerSelectionPoints().get(player.uniqueId());
        if (selectionPoints == null)
        {
            selectionPoints = new SelectionPoints(event.block().position(), null);
        }
        else
        {
            selectionPoints.setFirstPoint(event.block().position());
        }

        super.getPlugin().getPlayerSelectionPoints().put(player.uniqueId(), selectionPoints);
        player.sendMessage(LinearComponents.linear(Component.text("First point", NamedTextColor.GOLD),
                Component.text(" has been selected at ", NamedTextColor.BLUE),
                Component.text(event.block().position().toString(), NamedTextColor.GOLD)));
        event.setCancelled(true);
    }
}
