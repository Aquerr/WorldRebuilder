package io.github.aquerr.worldrebuilder.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;
import java.util.UUID;

public final class WorldUtils
{
    public static Optional<ServerWorld> getWorldByUUID(UUID worldUUID)
    {
        return Sponge.server().worldManager().worlds().stream()
                .filter(serverWorld -> serverWorld.uniqueId().equals(worldUUID))
                .findFirst();
    }

    private WorldUtils()
    {

    }
}
