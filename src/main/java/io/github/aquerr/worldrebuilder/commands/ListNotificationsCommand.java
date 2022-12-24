package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.model.Region;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class ListNotificationsCommand extends WRCommand
{
    public ListNotificationsCommand(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Region region = context.requireOne(Parameter.key("region", Region.class));
        final List<Component> helpList = new LinkedList<>();

        int number = 1;
        for (final Map.Entry<Long, String> notification : region.getNotifications().entrySet())
        {
            final Component component = LinearComponents.linear(
                    text(number + ". "), text("Time: ", BLUE), text(notification.getKey() + "s", GOLD), text(" Message: ", BLUE), text(notification.getValue(), GOLD)
            );
            helpList.add(component);
            number++;
        }

        final PaginationList paginationList = PaginationList.builder()
                .title(text("Notifications", GOLD))
                .contents(helpList).linesPerPage(10)
                .padding(text("-", BLUE))
                .build();
        paginationList.sendTo(context.cause().audience());
        return CommandResult.success();
    }
}
