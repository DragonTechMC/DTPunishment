package me.morpheus.dtpunishment.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import me.morpheus.dtpunishment.DTPunishment;

public class CommandReloadConfig implements CommandExecutor {

	private final DTPunishment main;

	public CommandReloadConfig(DTPunishment main) {
		this.main = main;
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		this.main.reloadConfiguration();
		return CommandResult.success();
	}

}
