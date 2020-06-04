package io.github.aquerr.worldrebuilder.listener;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlockPlaceListener extends AbstractListener
{
    public BlockPlaceListener(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Listener
    public void onBlockPlace(final ChangeBlockEvent.Place event)
    {
        // We should probably check here if it the WorldRebuilder that places blocks. If it is then we should not execute code below.

        final List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        final Collection<Region> regions = new ArrayList<>(super.getPlugin().getRegionManager().getRegions());

        for (final Transaction<BlockSnapshot> transaction : transactions)
        {
            final BlockSnapshot blockSnapshot = transaction.getFinal();
            for (final Region region : regions)
            {
                //All new blocks that are placed in a region should be treated as ignored blocks.
                if (region.intersects(blockSnapshot.getWorldUniqueId(), blockSnapshot.getPosition()))
                {
                    region.getBlockSnapshotsExceptions().put(blockSnapshot.getPosition(), blockSnapshot);
                    super.getPlugin().getRegionManager().updateRegion(region);
                }
            }
        }
    }
}
