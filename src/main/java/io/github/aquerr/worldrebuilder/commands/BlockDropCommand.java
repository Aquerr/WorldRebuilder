package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.commands.args.WorldRebuilderCommandParameters;
import io.github.aquerr.worldrebuilder.entity.Region;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

public class BlockDropCommand extends WRCommand
{
    public BlockDropCommand(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Region region = context.requireOne(WorldRebuilderCommandParameters.region());
        final boolean value = context.requireOne(Parameter.bool().key("value").build());

        region.setShouldDropBlocks(value);
        super.getPlugin().getRegionManager().updateRegion(region);
        context.sendMessage(Identity.nil(), WorldRebuilder.PLUGIN_PREFIX.append(Component.text("Region has been updated!", NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
