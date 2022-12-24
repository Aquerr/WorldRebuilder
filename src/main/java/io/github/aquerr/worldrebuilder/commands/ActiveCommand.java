package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

public class ActiveCommand extends WRCommand
{
    public ActiveCommand(final WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Region region = context.requireOne(Parameter.key("region", Region.class));
        final boolean isActive = context.requireOne(Parameter.bool().key("isActive").build());

        region.setActive(isActive);
        super.getPlugin().getRegionManager().updateRegion(region);
        context.sendMessage(Identity.nil(), WorldRebuilder.PLUGIN_PREFIX.append(Component.text("Region has been " + (isActive ? "activated" : "deactivated") + "!", NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
