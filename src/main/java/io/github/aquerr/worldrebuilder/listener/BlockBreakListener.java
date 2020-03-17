package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildBlocksTask;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.LocatableBlock;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BlockBreakListener extends AbstractListener
{

	public BlockBreakListener(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Listener(order = Order.LAST)
	public void onBlockBreak(final ChangeBlockEvent.Break event)
	{
		LocatableBlock locatableBlock = null;
		if(event.getSource() instanceof LocatableBlock)
		{
			locatableBlock = (LocatableBlock) event.getSource();
		}
		if(locatableBlock != null)
		{
			if(locatableBlock.getBlockState().getType() == BlockTypes.FLOWING_WATER || locatableBlock.getBlockState().getType() == BlockTypes.FLOWING_LAVA)
				return;
		}

		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		if(regions.size() == 0)
			return;

		final List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
		final UUID worldUUID = transactions.get(0).getOriginal().getWorldUniqueId();

		CompletableFuture.runAsync(() -> rebuildBlocks(worldUUID, new LinkedList<>(transactions)));
	}

	@Listener
	public void onEntitySpawn(final SpawnEntityEvent event)
	{
		final EventContext eventContext = event.getContext();
		final Object source = event.getSource();
		final boolean isBlockSource = source instanceof BlockSnapshot;
		final boolean isDroppedItem = eventContext.get(EventContextKeys.SPAWN_TYPE).isPresent() && eventContext.get(EventContextKeys.SPAWN_TYPE).get() == SpawnTypes.DROPPED_ITEM;
		final boolean isDroppedArmorStand = eventContext.get(EventContextKeys.SPAWN_TYPE).isPresent() && eventContext.get(EventContextKeys.SPAWN_TYPE).get() == SpawnTypes.PLACEMENT;
		final boolean isPlayerPlace = eventContext.get(EventContextKeys.PLAYER_PLACE).isPresent();

		if (isPlayerPlace)
			return;

		if (!isDroppedArmorStand && (!isDroppedItem || !isBlockSource))
			return;

		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		for (final Region region : regions)
		{
			event.filterEntities(x-> !(!region.shouldDropBlocks() && region.intersects(x.getWorld().getUniqueId(), x.getLocation().getBlockPosition())));
		}
	}

	@Listener
	public void onHangingDrop(final DropItemEvent.Pre event)
	{
		final Cause cause = event.getCause();

		//If it is itemframe/painting
		final boolean isHanging = cause.first(Hanging.class).isPresent();
		if (!isHanging)
			return;

		boolean isItemFrameDropped = false;
		for (final ItemStackSnapshot item : event.getDroppedItems())
		{
			if (item.getType() == ItemTypes.ITEM_FRAME || item.getType() == ItemTypes.PAINTING)
			{
				isItemFrameDropped = true;
				break;
			}
		}

		if (!isItemFrameDropped)
			return;

		final Hanging hanging = cause.first(Hanging.class).get();
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		for (final Region region : regions)
		{
			if (!region.isActive())
				continue;
			if (!region.shouldDropBlocks() && region.intersects(hanging.getWorld().getUniqueId(), hanging.getLocation().getBlockPosition()))
			{
				event.setCancelled(true);
				break;
			}
		}
	}

	private void rebuildBlocks(final UUID worldUUID, final List<Transaction<BlockSnapshot>> transactions)
	{
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		final List<BlockSnapshot> blocksToRestore = new LinkedList<>();
		Region affectedRegion = null;

		for(final Region region : regions)
		{
			if (!region.isActive())
				continue;

			for(final Transaction<BlockSnapshot> transaction : transactions)
			{
				if(region.intersects(worldUUID, transaction.getOriginal().getPosition()))
				{
					blocksToRestore.add(transaction.getOriginal());
					affectedRegion = region;
				}
			}
		}

		if (affectedRegion == null)
			return;

		super.getPlugin().getWorldRebuilderScheduler().scheduleRebuildBlocksTask(new RebuildBlocksTask(worldUUID, blocksToRestore), affectedRegion.getRestoreTime());
	}
}
