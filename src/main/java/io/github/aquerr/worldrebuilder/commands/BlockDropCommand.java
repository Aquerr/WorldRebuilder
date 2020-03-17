package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class BlockDropCommand extends WRCommand
{
    public BlockDropCommand(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
    {
        final Region region = args.requireOne(Text.of("region"));
        final boolean value = args.requireOne(Text.of("value"));

        region.setShouldDropBlocks(value);
        super.getPlugin().getRegionManager().updateRegion(region);
        src.sendMessage(Text.of(WorldRebuilder.PLUGIN_PREFIX, TextColors.GREEN, "Region has been updated!"));
        return CommandResult.success();
    }
}
