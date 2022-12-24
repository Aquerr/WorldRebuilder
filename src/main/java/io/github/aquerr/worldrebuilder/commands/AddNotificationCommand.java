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

public class AddNotificationCommand extends WRCommand
{
    public AddNotificationCommand(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Region region = context.requireOne(Parameter.key("region", Region.class));
        final Duration timeBeforeRebuild = context.requireOne(Parameter.duration().key("timeBeforeRebuild").build());
        final String message = context.requireOne(Parameter.string().key("message").build());

        region.getNotifications().put(timeBeforeRebuild.getSeconds(), message);
        super.getPlugin().getRegionManager().updateRegion(region);
        context.cause().audience().sendMessage(WorldRebuilder.PLUGIN_PREFIX.append(text("Notification has been added.", NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
