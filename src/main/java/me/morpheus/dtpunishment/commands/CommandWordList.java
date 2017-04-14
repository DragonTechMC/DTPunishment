package me.morpheus.dtpunishment.commands;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.configuration.ChatConfig;
import me.morpheus.dtpunishment.utils.Util;

public class CommandWordList implements CommandExecutor {

	private ChatConfig chatConfig;

	@Inject
	public CommandWordList(ChatConfig chatConfig) {
		this.chatConfig = chatConfig;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		PaginationList.Builder builder = PaginationList.builder();
		List<Text> contents = new ArrayList<Text>();

		for (String s : chatConfig.banned.words.stream().sorted().collect(Collectors.toList())) {
			contents.add(Text.of(TextColors.RED, s));
		}

		builder.title(Util.withWatermark("Banned words list")).contents(contents).padding(Text.of("-")).build()
				.sendTo(src);

		return CommandResult.success();
	}
}
