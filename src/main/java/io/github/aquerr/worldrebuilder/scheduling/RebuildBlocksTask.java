package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.util.WorldUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RebuildBlocksTask implements WorldRebuilderTask
{
	protected final String regionName;
	protected final UUID worldUUID;
	protected final List<BlockSnapshot> blocks;
	protected ScheduledTask task;
	protected int delay;
	protected int interval;

	public RebuildBlocksTask(final String regionName, final UUID worldUUID, List<BlockSnapshot> blocks)
	{
		this.regionName = regionName;
		this.worldUUID = worldUUID;
		this.blocks = blocks;
	}

	@Override
	public void run()
	{
		final Optional<ServerWorld> optionalWorld = WorldUtils.getWorldByUUID(worldUUID);
		if(!optionalWorld.isPresent())
			return;
		final ServerWorld world = optionalWorld.get();

		for(final BlockSnapshot blockSnapshot : this.blocks)
		{
			world.restoreSnapshot(blockSnapshot, true, BlockChangeFlags.ALL);

			// Will the block spawn where player stands?
			// If so, teleport the player to safe location.
			Sponge.server().onlinePlayers().stream()
					.filter(player -> isPlayerAtBlock(player, blockSnapshot))
					.forEach(this::safeTeleportPlayer);
		}

		if (this.interval == 0)
		{
			WorldRebuilderScheduler.getInstance().removeTaskForRegion(regionName, this);
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
	public UUID getWorldUniqueId()
	{
		return this.worldUUID;
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
	public int getInterval()
	{
		return this.interval;
	}

	@Override
	public void setInterval(int intervalInSeconds)
	{
		this.interval = intervalInSeconds;
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

	protected boolean isPlayerAtBlock(final ServerPlayer player, BlockSnapshot blockSnapshot)
	{
		return player.position().toInt().equals(blockSnapshot.position());
	}

	protected void safeTeleportPlayer(ServerPlayer player)
	{
		player.setLocationAndRotation(Sponge.server().teleportHelper()
				.findSafeLocation(ServerLocation.of(player.world(), player.position()))
				.orElse(player.serverLocation()),
				player.rotation());
	}
}
