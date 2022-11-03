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
	private final String regionName;
	private final UUID worldUUID;
	private final List<BlockSnapshot> blocks;
	private ScheduledTask task;

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

		WorldRebuilderScheduler.getInstance().removeTaskForRegion(regionName, this);
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
	public boolean cancel()
	{
		return this.task.cancel();
	}

	public void setTask(ScheduledTask task)
	{
		this.task = task;
	}

	private boolean isPlayerAtBlock(final ServerPlayer player, BlockSnapshot blockSnapshot)
	{
		return player.position().toInt().equals(blockSnapshot.position());
	}

	private void safeTeleportPlayer(ServerPlayer player)
	{
		player.setLocationAndRotation(Sponge.server().teleportHelper()
				.findSafeLocation(ServerLocation.of(player.world(), player.position()))
				.orElse(player.serverLocation()),
				player.rotation());
	}
}
