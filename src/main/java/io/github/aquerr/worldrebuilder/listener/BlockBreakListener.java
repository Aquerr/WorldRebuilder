package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.RebuildBlocksTask;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BlockBreakListener extends AbstractListener
{
	private final Task.Builder taskBuilder;

	public BlockBreakListener(final WorldRebuilder plugin)
	{
		super(plugin);
		taskBuilder = Sponge.getScheduler().createTaskBuilder();
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

		final Map<String, Region> regions = super.getPlugin().getRegionManager().getRegions();
		if(regions.size() == 0)
			return;

		final List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
		final UUID worldUUID = transactions.get(0).getOriginal().getWorldUniqueId();

		CompletableFuture.runAsync(() -> rebuildBlocks(worldUUID, new LinkedList<>(transactions)));
	}

	private void rebuildBlocks(final UUID worldUUID, final List<Transaction<BlockSnapshot>> transactions)
	{
		final Map<String, Region> regions = super.getPlugin().getRegionManager().getRegions();
		final List<BlockSnapshot> blocksToRestore = new LinkedList<>();

		for(final Region region : regions.values())
		{
			for(final Transaction<BlockSnapshot> transaction : transactions)
			{
				if(region.intersects(transaction.getOriginal().getPosition()))
				{
					blocksToRestore.add(transaction.getOriginal());
				}
			}
		}

		final Region region = regions.values().stream().filter(x->x.intersects(transactions.get(0).getOriginal().getPosition())).findFirst().get();
		super.getPlugin().getWorldRebuilderScheduler().scheduleRebuildBlocksTask(new RebuildBlocksTask(worldUUID, blocksToRestore), region.getRestoreTime());
	}
}
