package io.github.aquerr.worldrebuilder.commands.args;

import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.managers.RegionManager;
import org.spongepowered.api.command.parameter.Parameter;

public class WorldRebuilderCommandParameters
{
    private static Parameter.Value<Region> REGION;

    public static void init(RegionManager regionManager)
    {
        REGION = Parameter.builder(Region.class)
                .key("region")
                .addParser(new RegionArgument.ValueParser(regionManager))
                .completer(new RegionArgument.Completer(regionManager))
                .build();
    }

    public static Parameter.Value<Region> region()
    {
        return REGION;
    }
}
