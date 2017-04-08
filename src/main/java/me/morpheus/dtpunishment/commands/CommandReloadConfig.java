package me.morpheus.dtpunishment.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.configuration.ConfigurationManager;

public class CommandReloadConfig implements CommandExecutor {

    @Inject
    private ConfigurationManager configurationManager;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        configurationManager.loadConfiguration();
        return CommandResult.success();
    }

}
