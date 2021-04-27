package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import net.minecraft.item.ItemHangingEntity;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.LocatableBlock;

import java.util.*;
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
		//DropItemEvent is handled in separate method.
		//However, this method is still triggered by broken armor stands.
		if (event instanceof DropItemEvent)
			return;

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

				if (region.intersects(x.getWorld().getUniqueId(), x.getLocation().getBlockPosition()))
				{
					for (final BlockSnapshot blockSnapshot : region.getBlockSnapshotsExceptions())
					{
						if (blockSnapshot.getPosition().equals(x.getLocation().getBlockPosition()))
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
		final Cause cause = event.getCause();

		//If it is itemframe/painting
		Optional<Hanging> optionalHanging = cause.first(Hanging.class);
		if (!optionalHanging.isPresent())
			return;

		//Dropped item from inside item frame bypasses the above code and we need to preform additional
		//check if the dropped item is really hanging entity.
		boolean isHangingItemDropped = false;
		for (final ItemStackSnapshot item : event.getDroppedItems())
		{
			final ItemType itemType = item.getType();
			if (itemType instanceof ItemHangingEntity)
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

			if (region.intersects(hanging.getWorld().getUniqueId(), hanging.getLocation().getBlockPosition()) && !region.isEntityIgnored(hanging))
			{
				event.setCancelled(true);
				break;
			}
		}
	}

	private void rebuildBlocks(final UUID worldUUID, final List<Transaction<BlockSnapshot>> transactions)
	{
		final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
		List<BlockSnapshot> blocksToRestore = new ArrayList<>();

		for(final Region region : regions)
		{
			boolean shouldRebuild = false;

			if (!region.isActive())
				continue;

			for(final Transaction<BlockSnapshot> transaction : transactions)
			{
				if(region.intersects(worldUUID, transaction.getOriginal().getPosition()))
				{
					// Check ignored blocks
					if (region.isBlockIgnored(transaction.getOriginal()))
					{
						region.removeIgnoredBlock(transaction.getOriginal());
						continue;
					}

					blocksToRestore.add(transaction.getOriginal());
					shouldRebuild = true;
				}
			}

			if (shouldRebuild)
			{
				super.getPlugin().getWorldRebuilderScheduler().scheduleRebuildBlocksTask(region.getName(), worldUUID, blocksToRestore, region.getRestoreTime());
				blocksToRestore = new ArrayList<>();
			}
		}
	}
}
