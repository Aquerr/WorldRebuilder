package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.util.TeleportUtils;
import io.github.aquerr.worldrebuilder.util.WorldUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RebuildBlocksTask implements WorldRebuilderTask
{
	protected final String regionName;
	protected final List<BlockSnapshot> blocks;
	protected ScheduledTask task;
	protected int delay;

	public RebuildBlocksTask(final String regionName, List<BlockSnapshot> blocks)
	{
		this.regionName = regionName;
		this.blocks = blocks;
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

		for(final BlockSnapshot blockSnapshot : this.blocks)
		{
			world.restoreSnapshot(blockSnapshot.position(), blockSnapshot, true, BlockChangeFlags.ALL);

			// Will the block spawn where player stands?
			// If so, teleport the player to safe location.

			TeleportUtils.safeTeleportPlayerIfAtLocation(blockSnapshot.position(), region);
		}

		WorldRebuilderScheduler.getInstance().removeTaskForRegion(regionName, this);
		if (region.getRebuildBlocksStrategy().doesRunContinuously())
		{
			// Reschedule with the latest settings
			region.rebuildBlocks(Collections.emptyList());
		}
	}

	@Override
	public String getRegionName()
	{
		return this.regionName;
	}

	@Override
	public List<Vector3i> getAffectedPositions()
	{
		return this.blocks.stream()
				.map(BlockSnapshot::position)
				.collect(Collectors.toList());
	}

	@Override
	public void setTask(ScheduledTask task)
	{
		this.task = task;
	}

	@Override
	public ScheduledTask getTask()
	{
		return this.task;
	}

	@Override
	public int getDelay()
	{
		return this.delay;
	}

	@Override
	public void setDelay(int delayInSeconds)
	{
		this.delay = delayInSeconds;
	}
}
