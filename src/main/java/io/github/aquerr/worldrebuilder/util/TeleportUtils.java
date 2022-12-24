package io.github.aquerr.worldrebuilder.util;

import io.github.aquerr.worldrebuilder.model.Region;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

public class TeleportUtils
{
    public static void safeTeleportPlayerIfAtLocation(Vector3i vector3i, Region region)
    {
        int heightRadius = Math.abs(region.getFirstPoint().y() - region.getSecondPoint().y());
        int widthRadius = (int)Math.sqrt(Math.pow(Math.abs(region.getFirstPoint().x()), 2) + Math.pow(Math.abs(region.getSecondPoint().z()), 2));

        Sponge.server().onlinePlayers().stream()
                .filter(player -> isPlayerAtBlock(vector3i, player))
                .forEach(serverPlayer -> safeTeleportPlayer(serverPlayer, heightRadius, widthRadius));
    }

    public static void safeTeleportPlayer(ServerPlayer player, int height, int width)
    {
        player.setLocationAndRotation(Sponge.server().teleportHelper()
                        .findSafeLocation(ServerLocation.of(player.world(), player.position()), height, width)
                        .orElse(player.serverLocation()),
                player.rotation());
    }

    private static boolean isPlayerAtBlock(Vector3i vector3i, final ServerPlayer player)
    {
        return player.position().toInt().equals(vector3i);
    }
}
