package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.entity.Region;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderScheduler;
import io.github.aquerr.worldrebuilder.scheduling.WorldRebuilderTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class SchedulerTasksCommand extends WRCommand
{
    public SchedulerTasksCommand(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Collection<Region> regions = super.getPlugin().getRegionManager().getRegions();
        final List<Component> helpList = new LinkedList<>();

        for(final Region region : regions)
        {
            List<WorldRebuilderTask> tasksForRegion = WorldRebuilderScheduler.getInstance().getTasksForRegion(region.getName());
            Component regionRecord = LinearComponents.linear(text(" - ", BLUE), text(region.getName(), GOLD));
            for (final WorldRebuilderTask worldRebuilderTask  : tasksForRegion)
            {
                final Component taskTooltipInfo = LinearComponents.linear(
                        text("Rebuild Time: ", BLUE), text(worldRebuilderTask.getDelay(), GOLD));

                regionRecord = regionRecord.append(newline()).append(text(worldRebuilderTask.getTask().name()))
                        .hoverEvent(taskTooltipInfo);
            }

            helpList.add(regionRecord);
        }

        final PaginationList paginationList = PaginationList.builder()
                .title(text("Scheduler tasks", GOLD))
                .contents(helpList)
                .padding(text("-", BLUE))
                .build();
        paginationList.sendTo(context.cause().audience());

        return CommandResult.success();
    }
}
