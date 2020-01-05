package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ActiveCommand extends WRCommand
{
    public ActiveCommand(final WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource src, final CommandContext args) throws CommandException
    {
        final Region region = args.requireOne(Text.of("region"));
        final boolean isActive = args.requireOne(Text.of("isActive"));

        region.setActive(isActive);
        super.getPlugin().getRegionManager().updateRegion(region);
        src.sendMessage(Text.of(WorldRebuilder.PLUGIN_PREFIX, TextColors.GREEN, "Region has been " + (isActive ? "activated" : "deactivated") + "!"));
        return CommandResult.success();
    }
}
