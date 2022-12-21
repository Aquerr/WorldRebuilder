package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RebuildRegionFromRandomBlockSetTask extends RebuildBlocksTask
{
    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    private final List<BlockState> blocksToUse;

    public RebuildRegionFromRandomBlockSetTask(String regionName, List<BlockSnapshot> blocksToRebuild, Set<BlockState> blocksToUse)
    {
        super(regionName, blocksToRebuild);
        this.blocksToUse = new ArrayList<>(blocksToUse);
    }

    @Override
    public void run()
    {
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
                    safeTeleportPlayerIfAtLocation(Vector3i.from(x, y, z));
                    world.setBlock(x, y, z, getRandomBlock());
                }
            }
        }

        for(final BlockSnapshot blockSnapshot : blocks)
        {
            world.setBlock(blockSnapshot.position(), getRandomBlock(), BlockChangeFlags.ALL);

            // Will the block spawn where player stands?
            // If so, teleport the player to safe location.
            safeTeleportPlayerIfAtLocation(blockSnapshot.position());
        }
    }

    private BlockState getRandomBlock()
    {
        int randomIndex = THREAD_LOCAL_RANDOM.nextInt(blocksToUse.size());
        return this.blocksToUse.get(randomIndex);
    }
}
