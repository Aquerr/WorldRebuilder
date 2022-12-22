package io.github.aquerr.worldrebuilder.scheduling;

import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.util.List;

public interface WorldRebuilderTask extends Runnable
{
    String getRegionName();

    List<Vector3i> getAffectedPositions();

    default boolean cancel()
    {
        return getTask().cancel();
    }

    void setTask(ScheduledTask task);

    ScheduledTask getTask();

    int getDelay();

    void setDelay(int delayInSeconds);

    static void safeTeleportPlayerIfAtLocation(Vector3i vector3i, Region region)
    {
        int heightRadius = Math.abs(region.getFirstPoint().y() - region.getSecondPoint().y());
        int widthRadius = (int)Math.sqrt(Math.pow(Math.abs(region.getFirstPoint().x()), 2) + Math.pow(Math.abs(region.getSecondPoint().z()), 2));

        Sponge.server().onlinePlayers().stream()
                .filter(player -> isPlayerAtBlock(vector3i, player))
                .forEach(serverPlayer -> safeTeleportPlayer(serverPlayer, heightRadius, widthRadius));
    }

    static void safeTeleportPlayer(ServerPlayer player, int height, int width)
    {
        player.setLocationAndRotation(Sponge.server().teleportHelper()
                        .findSafeLocation(ServerLocation.of(player.world(), player.position()), height, width)
                        .orElse(player.serverLocation()),
                player.rotation());
    }

    static boolean isPlayerAtBlock(Vector3i vector3i, final ServerPlayer player)
    {
        return player.position().toInt().equals(vector3i);
    }
}
