package me.morpheus.dtpunishment.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.morpheus.dtpunishment.DTPunishment;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import me.morpheus.dtpunishment.WordChecker;
import me.morpheus.dtpunishment.configuration.ChatConfig;
import me.morpheus.dtpunishment.utils.Util;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class CommandWordAdd implements CommandExecutor {

	private ChatConfig chatConfig = DTPunishment.getChatConfig();

	private File configDir = DTPunishment.getInstance().configDir;

	private WordChecker wordChecker = DTPunishment.getWordChecker();

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Collection<String> words = args.<String>getAll("word");
		List<String> actual = chatConfig.banned.words;

		if (words.size() == 0) {
			src.sendMessage(Util.withWatermark(TextColors.AQUA, "Please specify one or more words to add"));
			return CommandResult.empty();
		}

		ArrayList<String> wordsAdded = new ArrayList<String>();

		for (String word : words) {
			if (actual.contains(word)) {
				src.sendMessage(Util.withWatermark(TextColors.AQUA, "The word ", TextColors.RED, word, TextColors.AQUA,
						" is already in the banned word list"));
				continue;
			}

			wordsAdded.add(word);
			actual.add(word.toLowerCase());
		}

		// TODO: start - this should all be done via configuration manager
		File chatData = new File(configDir, "chat.conf");

		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setFile(chatData)
				.build();
		ConfigurationNode chatNode;

		final TypeToken<List<String>> token = new TypeToken<List<String>>() {
			private static final long serialVersionUID = 1L;
		};

		try {
			chatNode = loader.load();
			chatNode.getNode("banned", "words").setValue(token, actual);
			loader.save(chatNode);
		} catch (IOException | ObjectMappingException e) {
			e.printStackTrace();
		}
		// TODO: end

		if (wordsAdded.size() > 0) {
			src.sendMessage(Util.withWatermark(TextColors.AQUA, "You added the word(s) ", TextColors.RED,
					String.join(", ", wordsAdded), TextColors.AQUA, " to the banned words list"));

			// Rebuild the word list
			wordChecker.updateFromConfiguration();

			return CommandResult.success();
		}

		return CommandResult.empty();

	}

}
