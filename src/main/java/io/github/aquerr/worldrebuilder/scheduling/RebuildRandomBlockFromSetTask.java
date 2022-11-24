package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.util.WorldUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RebuildRandomBlockFromSetTask extends RebuildBlocksTask
{
    private static final ThreadLocalRandom THREAD_LOCAL_RANDOM = ThreadLocalRandom.current();

    private final List<BlockSnapshot> blocksToUse;

    public RebuildRandomBlockFromSetTask(String regionName, UUID worldUUID, List<BlockSnapshot> blocksToRebuild, Set<BlockSnapshot> blocksToUse)
    {
        super(regionName, worldUUID, blocksToRebuild);
        this.blocksToUse = new ArrayList<>(blocksToUse);
    }

    @Override
    public void run()
    {
        final Optional<ServerWorld> optionalWorld = WorldUtils.getWorldByUUID(worldUUID);
        if(!optionalWorld.isPresent())
            return;
        final ServerWorld world = optionalWorld.get();

        for(final BlockSnapshot blockSnapshot : blocks)
        {
            world.restoreSnapshot(getRandomBlock(), true, BlockChangeFlags.ALL);

            // Will the block spawn where player stands?
            // If so, teleport the player to safe location.
            Sponge.server().onlinePlayers().stream()
                    .filter(player -> isPlayerAtBlock(player, blockSnapshot))
                    .forEach(this::safeTeleportPlayer);
        }
    }

    private BlockSnapshot getRandomBlock()
    {
        int randomIndex = THREAD_LOCAL_RANDOM.nextInt(blocksToUse.size());
        return this.blocksToUse.get(randomIndex);
    }
}
