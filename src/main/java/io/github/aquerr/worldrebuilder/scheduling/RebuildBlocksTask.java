package io.github.aquerr.worldrebuilder.scheduling;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class RebuildBlocksTask implements WorldRebuilderTask
{
	private final String regionName;
	private final UUID worldUUID;
	private final List<BlockSnapshot> blocks;
	private Task task;

	public RebuildBlocksTask(final String regionName, final UUID worldUUID, List<BlockSnapshot> blocks)
	{
		this.regionName = regionName;
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
			world.setBlock(blockSnapshot.getPosition(), blockSnapshot.getExtendedState());

			// Will the block spawn where player stands?
			// If so, teleport the player to safe location.
			Sponge.getServer().getOnlinePlayers().stream()
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
				.map(BlockSnapshot::getPosition)
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

	public void setTask(Task task)
	{
		this.task = task;
	}

	private boolean isPlayerAtBlock(final Player player, BlockSnapshot blockSnapshot)
	{
		return player.getPosition().toInt().equals(blockSnapshot.getPosition());
	}

	private void safeTeleportPlayer(Player player)
	{
		player.setLocationAndRotationSafely(new Location<>(player.getWorld(), player.getPosition()), player.getRotation());
	}
}
