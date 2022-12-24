package io.github.aquerr.worldrebuilder.commands;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.commands.args.WorldRebuilderCommandParameters;
import io.github.aquerr.worldrebuilder.model.Region;
import io.github.aquerr.worldrebuilder.strategy.RebuildBlocksStrategy;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyType;
import io.github.aquerr.worldrebuilder.strategy.RebuildStrategyFactory;
import io.github.aquerr.worldrebuilder.strategy.WRBlockState;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.Collection;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class StrategyCommand extends WRCommand
{
    public StrategyCommand(WorldRebuilder plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Region region = context.requireOne(WorldRebuilderCommandParameters.region());
        final RebuildStrategyType strategy = context.requireOne(Parameter.enumValue(RebuildStrategyType.class).key("strategy").build());
        final Collection<? extends BlockState> blockList = context.all(Parameter.blockState().key("blockList").build());

        if (strategy.hasPredefinedBlockSet() && blockList == null)
        {
            throw new CommandException(WorldRebuilder.PLUGIN_ERROR.append(text("Selected rebuild strategy require predefined block set!")));
        }

        RebuildBlocksStrategy rebuildBlocksStrategy = RebuildStrategyFactory.getStrategy(strategy, blockList.stream().map(WRBlockState::of).collect(Collectors.toList()));
        region.setRebuildBlocksStrategy(rebuildBlocksStrategy);
        super.getPlugin().getRegionManager().updateRegion(region);
        context.sendMessage(Identity.nil(), WorldRebuilder.PLUGIN_PREFIX.append(Component.text("Rebuild blocks strategy has been updated!", NamedTextColor.GREEN)));
        return CommandResult.success();
    }
}
