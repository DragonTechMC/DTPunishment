package me.morpheus.dtpunishment.commands;

import me.morpheus.dtpunishment.DTPunishment;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.WordChecker;
import me.morpheus.dtpunishment.utils.Util;

public class CommandReloadConfig implements CommandExecutor {

	private CommandManager commandManager;

	private WordChecker wordChecker;

	public CommandReloadConfig() {
		this.wordChecker = DTPunishment.getWordChecker();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		DTPunishment.getInstance().onReload(null);
		DTPunishment.getCommandManager().registerCommands();
        src.sendMessage(Util.withWatermark(TextColors.AQUA, "Configuration has been reloaded"));

		// Rebuild the word list
		wordChecker.updateFromConfiguration();
		return CommandResult.success();
	}

}
