package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

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
    public void onBlockPlace(final ChangeBlockEvent.All event)
    {
        // If it is the WorldRebuilder that is placing (restoring) a block then don't execute code below.
        final Cause cause = event.cause();
        if (cause.contains(Sponge.pluginManager().fromInstance(super.getPlugin()).get()))
            return;

        final List<BlockTransaction> transactions = event.transactions();
        if (transactions.size() == 0)
            return;

        CompletableFuture.runAsync(() -> {
            final Collection<Region> regions = new ArrayList<>(super.getPlugin().getRegionManager().getRegions());
            for (final Region region : regions)
            {
                final List<BlockSnapshot> blocksToRestore = new LinkedList<>();
                for (final BlockTransaction transaction : transactions)
                {
                    if (!transaction.operation().equals(Operations.PLACE.get()))
                        continue;

                    if (region.intersects(transaction.original().location().get().world().uniqueId(), transaction.original().position()))
                    {
                        if (!region.isActive() && region.getRebuildBlocksStrategy().doesRunContinuously())
                        {
                            continue;
                        }

                        // If block was placed on air block.
                        if (transaction.original().state().type() == BlockTypes.AIR.get())
                        {
                            region.getBlockSnapshotsExceptions().add(transaction.finalReplacement());
                            super.getPlugin().getRegionManager().updateRegion(region);
                        }
                        else // Happens when dirt is converted to farmland and vice versa
                        {
                            blocksToRestore.add(transaction.original());
                        }
                    }
                }

                if (blocksToRestore.size() > 0)
                {
                    region.rebuildBlocks(blocksToRestore);
                }
            }
        });
    }
}
