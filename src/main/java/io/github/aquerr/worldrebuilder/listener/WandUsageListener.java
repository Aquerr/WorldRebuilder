package io.github.aquerr.worldrebuilder.listener;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.SelectionPoints;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class WandUsageListener extends AbstractListener
{
    public WandUsageListener(final WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Listener
    public void onRightClick(final InteractBlockEvent.Secondary event, final @Root Player player)
    {
        if(event.getHandType() == HandTypes.MAIN_HAND)
            return;

        if(event.getTargetBlock() == BlockSnapshot.NONE)
            return;

        if(!player.getItemInHand(HandTypes.MAIN_HAND).isPresent())
            return;

        final ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();

        if(!itemInHand.get(Keys.DISPLAY_NAME).isPresent() || !player.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).get().toPlain().equals("WorldRebuilder Wand"))
            return;

        SelectionPoints selectionPoints = super.getPlugin().getPlayerSelectionPoints().get(player.getUniqueId());
        if (selectionPoints == null)
        {
            selectionPoints = new SelectionPoints(null, event.getTargetBlock().getPosition());
        }
        else
        {
            selectionPoints.setSecondPoint(event.getTargetBlock().getPosition());
        }

        super.getPlugin().getPlayerSelectionPoints().put(player.getUniqueId(), selectionPoints);
        player.sendMessage(Text.of(TextColors.GOLD, "Second point", TextColors.BLUE, " has been selected at ", TextColors.GOLD, event.getTargetBlock().getPosition()));
    }

    @Listener
    public void onLeftClick(final InteractBlockEvent.Primary event, final @Root Player player)
    {
        if(event.getHandType() == HandTypes.OFF_HAND)
            return;

        if(event.getTargetBlock() == BlockSnapshot.NONE)
            return;

        if(!player.getItemInHand(HandTypes.MAIN_HAND).isPresent())
            return;

        final ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();

        if(!itemInHand.get(Keys.DISPLAY_NAME).isPresent() || !player.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).get().toPlain().equals("WorldRebuilder Wand"))
            return;

        SelectionPoints selectionPoints = super.getPlugin().getPlayerSelectionPoints().get(player.getUniqueId());
        if (selectionPoints == null)
        {
            selectionPoints = new SelectionPoints(event.getTargetBlock().getPosition(), null);
        }
        else
        {
            selectionPoints.setFirstPoint(event.getTargetBlock().getPosition());
        }

        super.getPlugin().getPlayerSelectionPoints().put(player.getUniqueId(), selectionPoints);
        player.sendMessage(Text.of(TextColors.GOLD, "First point", TextColors.BLUE, " has been selected at ", TextColors.GOLD, event.getTargetBlock().getPosition()));
    }

//    @Listener
//    public void onRightClickTest(final InteractBlockEvent.Secondary event, final @Root Player player)
//    {
//        if(event.getHandType() == HandTypes.MAIN_HAND)
//            return;
//
//        if(event.getTargetBlock() == BlockSnapshot.NONE)
//            return;
//
//        final World world = player.getWorld();
//        final Direction direction = event.getTargetSide();
//        final Entity itemFrame = world.createEntity(EntityTypes.ITEM_FRAME, getPaintingLocation(event.getInteractionPoint().get(), direction));
//        itemFrame.offer(Keys.DIRECTION, direction);
//        world.spawnEntity(itemFrame);
//    }
//
//    private Vector3d getPaintingLocation(final Vector3d blockPosition, final Direction direction)
//    {
//        switch(direction)
//        {
//            case EAST:
//                return blockPosition.add(0.1, 0, 0);
//            case WEST:
//                return blockPosition.add(-0.1, 0, 0);
//            case NORTH:
//                return blockPosition.add(0, 0, -0.1);
//            case SOUTH:
//                return blockPosition.add(0.1, 0, 0.1);
//        }
//
//        return Vector3d.ZERO;
//    }
}
