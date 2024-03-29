package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.util.TeleportUtils;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.List;
import java.util.Optional;

/**
 * This task is meant to run every second.
 *
 * It displays information in the chat about remaining time to next rebuild.
 * Notifications can be configured by the user. By default, notification is displayed once, 10 seconds before rebuild.
 */
public class ConstantRebuildRegionBlocksTask extends RebuildBlocksTask
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantRebuildRegionBlocksTask.class);

    private final Region region;
    private int currentSeconds;

    public ConstantRebuildRegionBlocksTask(Region region, List<BlockSnapshot> originalRegionBlocks, final int rebuildTimeSeconds)
    {
        super(region.getName(), originalRegionBlocks);
        this.region = region;
        this.delay = rebuildTimeSeconds;
        this.currentSeconds = delay;
    }

    @Override
    public void run()
    {
        try
        {
            if (this.currentSeconds > 0)
            {
                displayNotificationIfNecessary(this.currentSeconds);
                this.currentSeconds--;
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
            resetTime();
        }
        catch (Exception exception)
        {
            LOGGER.error("Could not rebuild region.", exception);
        }
    }

    private void resetTime()
    {
        this.currentSeconds = delay;
    }

    private void displayNotificationIfNecessary(int secondsLeft)
    {
        String message = this.region.getNotifications().get((long)secondsLeft);
        if (message != null)
        {
            Sponge.server().broadcastAudience().sendMessage(Identity.nil(),
                    LinearComponents.linear(WorldRebuilder.PLUGIN_PREFIX, LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(message.replaceAll("\\{REGION_NAME}", regionName))));
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
                if (serverWorld.hasBlockState(blockSnapshot.position(), blockState -> blockSnapshot.state().equals(blockState)))
                    continue;

                serverWorld.restoreSnapshot(blockSnapshot.position(), blockSnapshot, true, BlockChangeFlags.DEFAULT_PLACEMENT);

                // Will the block spawn where player stands?
                // If so, teleport the player to safe location.
                TeleportUtils.safeTeleportPlayerIfAtLocation(blockSnapshot.position(), region);
            }
        }
    }
}
