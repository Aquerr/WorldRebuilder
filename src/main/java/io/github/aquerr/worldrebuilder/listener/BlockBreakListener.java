package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import net.minecraft.item.HangingEntityItem;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.LocatableBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BlockBreakListener extends AbstractListener
{

	public BlockBreakListener(final WorldRebuilder plugin)
	{
		super(plugin);
	}

	@Listener
	public void onBlockBreak(final ChangeBlockEvent.All event)
	{
		LocatableBlock locatableBlock = null;
		if(event.source() instanceof LocatableBlock)
		{
			locatableBlock = (LocatableBlock) event.source();
		}
		if(locatableBlock != null)
		{
			if(locatableBlock.blockState().type() == BlockTypes.WATER.get() || locatableBlock.blockState().type() == BlockTypes.LAVA.get())
				return;
		}

		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		if(regions.size() == 0)
			return;

		final List<BlockTransaction> transactions = event.transactions();
		final List<BlockTransaction> filteredTransactions = new ArrayList<>();
		for (final BlockTransaction blockTransaction : transactions)
		{
			if (blockTransaction.operation().equals(Operations.BREAK.get()))
				filteredTransactions.add(blockTransaction);
		}

		if (filteredTransactions.isEmpty())
			return;

		CompletableFuture.runAsync(() -> rebuildBlocks(new LinkedList<>(filteredTransactions)));
	}

	@Listener
	public void onEntitySpawn(final SpawnEntityEvent event)
	{
		//DropItemEvent is handled in separate method.
		//However, this method is still triggered by broken armor stands.
		if (event instanceof DropItemEvent)
			return;

		final EventContext eventContext = event.context();
		final Object source = event.source();
		final boolean isBlockSource = source instanceof BlockSnapshot;
		final boolean isDroppedItem = eventContext.get(EventContextKeys.SPAWN_TYPE).isPresent() && eventContext.get(EventContextKeys.SPAWN_TYPE).get() == SpawnTypes.DROPPED_ITEM.get();
		final boolean isDroppedArmorStand = eventContext.get(EventContextKeys.SPAWN_TYPE).isPresent() && eventContext.get(EventContextKeys.SPAWN_TYPE).get() == SpawnTypes.PLACEMENT.get();
		final boolean isPlayerPlace = eventContext.get(EventContextKeys.PLAYER_PLACE).isPresent();

		if (isPlayerPlace)
			return;

		if (!isDroppedArmorStand && (!isDroppedItem || !isBlockSource))
			return;

		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		for (final Region region : regions)
		{
			event.filterEntities(x-> !(!region.shouldDropBlocks() && region.intersects(x.serverLocation().world().uniqueId(), x.serverLocation().blockPosition())));
		}
	}

	/*
	 * Handles all block drops. (Does not include armor stands, item frames and paintings)
	 */
	@Listener(order = Order.FIRST, beforeModifications = true)
	public void onBlockDrop(final DropItemEvent.Destruct event)
	{
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		event.filterEntities(x->
		{
			for (final Region region : regions)
			{
				if (!region.isActive())
					continue;

				if (region.shouldDropBlocks())
					continue;

				if (region.intersects(x.serverLocation().world().uniqueId(), x.serverLocation().blockPosition()))
				{
					for (final BlockSnapshot blockSnapshot : region.getBlockSnapshotsExceptions())
					{
						if (blockSnapshot.position().equals(x.serverLocation().blockPosition()))
						{
							return true;
						}
					}

					return false;
				}
			}
			return true;
		});
	}

	/*
	 * Handles dropped hanging entities.
	 */
	@Listener(order = Order.FIRST, beforeModifications = true)
    public void onHangingDrop(final DropItemEvent.Pre event)
	{
		final Cause cause = event.cause();

		//If it is itemframe/painting
		Optional<Hanging> optionalHanging = cause.first(Hanging.class);
		if (!optionalHanging.isPresent())
			return;

		//Dropped item from inside item frame bypasses the above code and we need to preform additional
		//check if the dropped item is really hanging entity.
		boolean isHangingItemDropped = false;
		for (final ItemStackSnapshot item : event.droppedItems())
		{
			final ItemType itemType = item.type();
			if (itemType instanceof HangingEntityItem)
			{
				isHangingItemDropped = true;
				break;
			}
		}

		//Items that were put inside item frames can be dropped. The question is... is that correct or should be block it as well?
		if (!isHangingItemDropped)
			return;

		//At this point we are sure that hanging entity is dropped.
		final Hanging hanging = optionalHanging.get();
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		for (final Region region : regions)
		{
			if (!region.isActive())
				continue;

			if (region.shouldDropBlocks())
				continue;

			if (region.intersects(hanging.serverLocation().world().uniqueId(), hanging.serverLocation().blockPosition()) && !region.isEntityIgnored(hanging))
			{
				event.setCancelled(true);
				break;
			}
		}
	}

	private void rebuildBlocks(final List<BlockTransaction> transactions)
	{
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		for(final Region region : regions)
		{
			region.rebuildBlocks(transactions.stream()
					.map(Transaction::original)
					.collect(Collectors.toList()));
		}
	}
}
