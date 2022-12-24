package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.time.Duration;

import static net.kyori.adventure.text.Component.text;

public class DeleteNotificationCommand extends WRCommand
{
    public DeleteNotificationCommand(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Region region = context.requireOne(Parameter.key("region", Region.class));
        final Duration timeBeforeRebuild = context.requireOne(Parameter.duration().key("timeBeforeRebuild").build());

        region.getNotifications().remove(timeBeforeRebuild.getSeconds());
        super.getPlugin().getRegionManager().updateRegion(region);
        context.cause().audience().sendMessage(WorldRebuilder.PLUGIN_PREFIX.append(text("Notification has been removed.", NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
