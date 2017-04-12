package me.morpheus.dtpunishment.commands;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import me.morpheus.dtpunishment.configuration.ChatConfig;
import me.morpheus.dtpunishment.utils.Util;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class CommandWordAdd implements CommandExecutor {

    @Inject
    private ChatConfig chatConfig;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        String word = args.<String>getOne("word").get();
        List<String> actual = chatConfig.banned.words;

        if (actual.contains(word)) {
            src.sendMessage(Util.getWatermark().append(Text.of(TextColors.AQUA, "The word ", TextColors.RED, word,
                    TextColors.AQUA, " is already in the banned word list")).build());

            return CommandResult.empty();
        }

        actual.add(word.toLowerCase());

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

        src.sendMessage(Util.getWatermark().append(Text.of(TextColors.AQUA, "You added ", TextColors.RED, word,
                TextColors.AQUA, " to the banned word list")).build());

        return CommandResult.success();

    }

}
