package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BlockPlaceListener extends AbstractListener
{
    public BlockPlaceListener(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Listener
    public void onBlockPlace(final ChangeBlockEvent.Place event)
    {
        // If it is the WorldRebuilder that is placing (restoring) a block then don't execute code below.
        final Cause cause = event.getCause();
        if (cause.contains(Sponge.getPluginManager().fromInstance(super.getPlugin()).get()))
            return;

        final List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        if (transactions.size() == 0)
            return;

        CompletableFuture.runAsync(() -> {
            final Collection<Region> regions = new ArrayList<>(super.getPlugin().getRegionManager().getRegions());
            for (final Region region : regions)
            {
                final List<BlockSnapshot> blocksToRestore = new LinkedList<>();
                for (final Transaction<BlockSnapshot> transaction : transactions)
                {
                    if (region.intersects(transaction.getOriginal().getWorldUniqueId(), transaction.getOriginal().getPosition()))
                    {
                        if (!region.isActive())
                        {
                            continue;
                        }

                        // If block was placed on air block.
                        if (transaction.getOriginal().getState().getType() == BlockTypes.AIR)
                        {
                            region.getBlockSnapshotsExceptions().add(transaction.getFinal());
                            super.getPlugin().getRegionManager().updateRegion(region);
                        }
                        else // Happens when dirt is converted to farmland and vice versa
                        {
                            blocksToRestore.add(transaction.getOriginal());
                        }
                    }
                }

                if (blocksToRestore.size() > 0)
                {
                    super.getPlugin().getWorldRebuilderScheduler().scheduleRebuildBlocksTask(region.getName(), transactions.get(0).getOriginal().getWorldUniqueId(), blocksToRestore, region.getRestoreTime());
                }
            }
        });
    }
}
