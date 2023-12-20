package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.time.Duration;

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
        final Component message = context.requireOne(Parameter.formattingCodeText().key("message").build());

        region.getNotifications().put(timeBeforeRebuild.getSeconds(), LegacyComponentSerializer.legacyAmpersand().serialize(message));
        super.getPlugin().getRegionManager().updateRegion(region);
        context.cause().audience().sendMessage(getPlugin().getMessageSource().resolveMessageWithPrefix("command.region.notification.add"));
        return CommandResult.success();
    }
}
