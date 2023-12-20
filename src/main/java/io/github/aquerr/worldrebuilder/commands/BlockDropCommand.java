package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.commands.args.WorldRebuilderCommandParameters;
import io.github.aquerr.worldrebuilder.model.Region;
import net.kyori.adventure.identity.Identity;
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
        context.sendMessage(Identity.nil(), getPlugin().getMessageSource().resolveMessageWithPrefix("command.region.updated"));
        return CommandResult.success();
    }
}
