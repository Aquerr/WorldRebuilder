package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.strategy.WRBlockState;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static net.kyori.adventure.text.Component.text;

/**
 * This task is meant to run every second.
 *
 * It prints information messages about rebuild when will take place.
 * Times can be specified by the user. By default, message is displayed once 10 seconds before rebuild.
 */
public class ConstantRebuildRegionFromRandomBlockSetTask extends RebuildBlocksTask
{
    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    private final List<WRBlockState> blocksToUse;

    private int currentSeconds;

    public ConstantRebuildRegionFromRandomBlockSetTask(String regionName, List<BlockSnapshot> blocksToRebuild, List<WRBlockState> blocksToUse, final int rebuildTimeSeconds)
    {
        super(regionName, blocksToRebuild);
        this.blocksToUse = new ArrayList<>(blocksToUse);
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

        int minimumX = Math.min(region.getFirstPoint().x(), region.getSecondPoint().x());
        int minimumY = Math.min(region.getFirstPoint().y(), region.getSecondPoint().y());
        int minimumZ = Math.min(region.getFirstPoint().z(), region.getSecondPoint().z());
        int maximumX = Math.max(region.getFirstPoint().x(), region.getSecondPoint().x());
        int maximumY = Math.max(region.getFirstPoint().y(), region.getSecondPoint().y());
        int maximumZ = Math.max(region.getFirstPoint().z(), region.getSecondPoint().z());

        for (int x = minimumX; x <= maximumX; x++)
        {
            for (int z = minimumZ; z <= maximumZ; z++)
            {
                for (int y = minimumY; y <= maximumY; y++)
                {
                    world.setBlock(x, y, z, getRandomBlock().getBlockState());
                    safeTeleportPlayerIfAtLocation(Vector3i.from(x, y, z), region);
                }
            }
        }

        for(final BlockSnapshot blockSnapshot : blocks)
        {
            world.setBlock(blockSnapshot.position(), getRandomBlock().getBlockState(), BlockChangeFlags.ALL);

            // Will the block spawn where player stands?
            // If so, teleport the player to safe location.
            safeTeleportPlayerIfAtLocation(blockSnapshot.position(), region);
        }

        // Reschedule with the latest settings
        WorldRebuilderScheduler.getInstance().cancelTasksForRegion(region.getName());
        region.rebuildBlocks(Collections.emptyList());
    }

    private WRBlockState getRandomBlock()
    {
        int randomIndex = THREAD_LOCAL_RANDOM.nextInt(blocksToUse.size());
        return this.blocksToUse.get(randomIndex);
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
                            text(secondsLeft).color(NamedTextColor.GOLD)));
        }
    }
}
