package me.morpheus.dtpunishment.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.WordChecker;
import me.morpheus.dtpunishment.configuration.ConfigurationManager;
import me.morpheus.dtpunishment.utils.Util;

public class CommandReloadConfig implements CommandExecutor {

	private ConfigurationManager configurationManager;

	private CommandManager commandManager;

	private WordChecker wordChecker;

	@Inject
	public CommandReloadConfig(ConfigurationManager configurationManager, WordChecker wordChecker,
			CommandManager commandManager) {
		this.configurationManager = configurationManager;
		this.wordChecker = wordChecker;
		this.commandManager = commandManager;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		configurationManager.loadConfiguration();

		// Rebuild the word list
		wordChecker.buildWordList();

		// re-register commands in case they changed
		commandManager.registerCommands();

		src.sendMessage(Util.withWatermark(TextColors.AQUA, "Configuration has been reloaded"));

		return CommandResult.success();
	}

}
