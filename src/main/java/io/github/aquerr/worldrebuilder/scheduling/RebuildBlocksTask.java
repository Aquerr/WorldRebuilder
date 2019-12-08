package io.github.aquerr.worldrebuilder.scheduling;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RebuildBlocksTask implements Runnable
{
	private final UUID worldUUID;
	private final List<BlockSnapshot> blocks;

	public RebuildBlocksTask(final UUID worldUUID, List<BlockSnapshot> blocks)
	{
		this.worldUUID = worldUUID;
		this.blocks = blocks;
	}

	@Override
	public void run()
	{
		final Optional<World> optionalWorld = Sponge.getServer().getWorld(worldUUID);
		if(!optionalWorld.isPresent())
			return;
		final World world = optionalWorld.get();

		for(final BlockSnapshot blockSnapshot : this.blocks)
		{
			world.setBlock(blockSnapshot.getPosition(), blockSnapshot.getState());
		}
	}
}
