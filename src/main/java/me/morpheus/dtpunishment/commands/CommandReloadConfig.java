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

	private WordChecker wordChecker;

	@Inject
	public CommandReloadConfig(ConfigurationManager configurationManager, WordChecker wordChecker) {
		this.configurationManager = configurationManager;
		this.wordChecker = wordChecker;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		configurationManager.loadConfiguration();

		// Rebuild the word list
		wordChecker.buildWordList();

		src.sendMessage(Util.withWatermark(TextColors.AQUA, "Configuration has been reloaded"));

		return CommandResult.success();
	}

}
