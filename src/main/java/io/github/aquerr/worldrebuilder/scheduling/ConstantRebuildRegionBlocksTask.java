package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.kyori.adventure.text.Component.text;

/**
 * This task is meant to run every second.
 *
 * It prints information messages about rebuild when will take place.
 * Times can be specified by the user. By default, message is displayed once 10 seconds before rebuild.
 */
public class ConstantRebuildRegionBlocksTask extends RebuildBlocksTask
{
    private int currentSeconds;

    public ConstantRebuildRegionBlocksTask(String regionName, List<BlockSnapshot> originalRegionBlocks, final int rebuildTimeSeconds)
    {
        super(regionName, originalRegionBlocks);
        this.delay = rebuildTimeSeconds;
        this.currentSeconds = delay;
    }

    @Override
    public void run()
    {
        if (this.currentSeconds > 0)
        {
            this.currentSeconds--;
            displayRebuildMessageIfNecessary(this.currentSeconds);
            return;
        }

        Region region = WorldRebuilder.getPlugin().getRegionManager().getRegion(regionName);
        if (!region.isActive())
            return;

        final Optional<ServerWorld> optionalWorld = WorldUtils.getWorldByUUID(region.getWorldUniqueId());
        if(!optionalWorld.isPresent())
            return;
        final ServerWorld world = optionalWorld.get();

        WorldRebuilderScheduler.getInstance().scheduleTask(new DoRebuild(region, world, blocks));
        WorldRebuilderScheduler.getInstance().cancelTasksForRegion(regionName);
        region.rebuildBlocks(Collections.emptyList());
    }

    private void displayRebuildMessageIfNecessary(int secondsLeft)
    {
        if (secondsLeft == 10)
        {
            Sponge.server().broadcastAudience().sendMessage(Identity.nil(),
                    LinearComponents.linear(WorldRebuilder.PLUGIN_PREFIX,
                            text("Region "),
                            text(regionName).color(NamedTextColor.BLUE),
                            text(" will be rebuild in "),
                            text(secondsLeft + " seconds").color(NamedTextColor.GOLD)));
        }
    }

    private static class DoRebuild implements Runnable
    {
        private final Region region;
        private final ServerWorld serverWorld;
        private final List<BlockSnapshot> blockSnapshots;

        DoRebuild(Region region, final ServerWorld serverWorld, final List<BlockSnapshot> blockSnapshots)
        {
            this.region = region;
            this.serverWorld = serverWorld;
            this.blockSnapshots = blockSnapshots;
        }

        @Override
        public void run()
        {
            for(final BlockSnapshot blockSnapshot : this.blockSnapshots)
            {
                serverWorld.restoreSnapshot(blockSnapshot.position(), blockSnapshot, true, BlockChangeFlags.ALL);

                // Will the block spawn where player stands?
                // If so, teleport the player to safe location.
                WorldRebuilderTask.safeTeleportPlayerIfAtLocation(blockSnapshot.position(), region);
            }
        }
    }
}
