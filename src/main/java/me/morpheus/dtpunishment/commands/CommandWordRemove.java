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

public class CommandWordRemove implements CommandExecutor {

	private ChatConfig chatConfig;

	private File configDir;

	private WordChecker wordChecker;

	public CommandWordRemove() {
		this.chatConfig = DTPunishment.getChatConfig();
		this.configDir = DTPunishment.getInstance().configDir;
		this.wordChecker = DTPunishment.getWordChecker();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Collection<String> words = args.<String>getAll("word");
		List<String> actual = chatConfig.banned.words;

		if (words.size() == 0) {
			src.sendMessage(Util.withWatermark(TextColors.AQUA, "Please specify one or more words to remove"));
			return CommandResult.empty();
		}

		ArrayList<String> wordsRemoved = new ArrayList<String>();

		for (String word : words) {
			if (!actual.contains(word)) {
				src.sendMessage(Util.withWatermark(TextColors.AQUA, "The word ", TextColors.RED, word, TextColors.AQUA,
						" is not in the banned word list"));
				continue;
			}

			wordsRemoved.add(word);
			actual.remove(word.toLowerCase());
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
            DTPunishment.getInstance().onReload(null);
		} catch (IOException | ObjectMappingException e) {
			e.printStackTrace();
		}
		// TODO: end

		if (wordsRemoved.size() > 0) {
			src.sendMessage(Util.withWatermark(TextColors.AQUA, "You removed the word(s) ", TextColors.RED,
					String.join(", ", wordsRemoved), TextColors.AQUA, " from the banned word list"));

			// Rebuild the word list
			wordChecker.updateFromConfiguration();

			return CommandResult.success();
		}

		return CommandResult.empty();
	}

}
