package me.morpheus.dtpunishment.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
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

	private Path configDir;

	private WordChecker wordChecker;

	@Inject
	public CommandWordRemove(ChatConfig chatConfig, @ConfigDir(sharedRoot = false) Path configDir,
			WordChecker wordChecker) {
		this.chatConfig = chatConfig;
		this.configDir = configDir;
		this.wordChecker = wordChecker;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		Collection<String> words = args.<String>getAll("word");
		List<String> actual = chatConfig.banned.words;

		if (words.size() == 0) {
			src.sendMessage(Util.withWatermark(TextColors.AQUA, "Please specify one or more words to remove"));
			return CommandResult.empty();
		}

		for (String word : words) {
			if (!actual.contains(word)) {
				src.sendMessage(Util.withWatermark(TextColors.AQUA, "The word ", TextColors.RED, word, TextColors.AQUA,
						" is not in the banned word list"));
			}

			actual.remove(word.toLowerCase());
		}

		// TODO: start - this should all be done via configuration manager
		Path chatData = configDir.resolve("chat.conf");

		ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(chatData)
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

		src.sendMessage(Util.withWatermark(TextColors.AQUA, "You removed the word(s) ", TextColors.RED,
				String.join(", ", words), TextColors.AQUA, " from the banned word list"));

		// Rebuild the word list
		wordChecker.buildWordList();

		return CommandResult.success();

	}

}
