package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

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
                safeTeleportPlayerIfAtLocation(blockSnapshot.position(), region);
            }
        }

        protected void safeTeleportPlayerIfAtLocation(Vector3i vector3i, Region region)
        {
            int heightRadius = Math.abs(region.getFirstPoint().y() - region.getSecondPoint().y());
            int widthRadius = (int)Math.sqrt(Math.pow(Math.abs(region.getFirstPoint().x()), 2) + Math.pow(Math.abs(region.getSecondPoint().z()), 2));

            Sponge.server().onlinePlayers().stream()
                    .filter(player -> isPlayerAtBlock(vector3i, player))
                    .forEach(serverPlayer -> safeTeleportPlayer(serverPlayer, heightRadius, widthRadius));
        }

        private void safeTeleportPlayer(ServerPlayer player, int height, int width)
        {
            player.setLocationAndRotation(Sponge.server().teleportHelper()
                            .findSafeLocation(ServerLocation.of(player.world(), player.position()), height, width)
                            .orElse(player.serverLocation()),
                    player.rotation());
        }

        protected boolean isPlayerAtBlock(Vector3i vector3i, final ServerPlayer player)
        {
            return player.position().toInt().equals(vector3i);
        }
    }
}
