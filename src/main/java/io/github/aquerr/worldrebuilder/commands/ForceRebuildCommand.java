package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.commands.args.WorldRebuilderCommandParameters;
import io.github.aquerr.worldrebuilder.model.Region;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class ForceRebuildCommand extends WRCommand
{
    public ForceRebuildCommand(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Region region = context.requireOne(WorldRebuilderCommandParameters.region());
        super.getPlugin().getRegionManager().forceRebuildRegion(region);
        context.sendMessage(Identity.nil(), WorldRebuilder.PLUGIN_PREFIX.append(Component.text("Region has been rebuilt!", NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
