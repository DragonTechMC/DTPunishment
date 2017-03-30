package me.morpheus.dtpunishment.commands;

import com.google.common.reflect.TypeToken;
import me.morpheus.dtpunishment.DTPunishment;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CommandWordAdd implements CommandExecutor {

    private final DTPunishment main;

    public CommandWordAdd(DTPunishment main){
        this.main = main;
    }


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        String word = args.<String>getOne("word").get();
        List<String> actual = main.getChatConfig().banned.words;
        actual.add(word);


        Path chatData = Paths.get(main.getConfigPath() + "/chat.conf");

        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(chatData).build();
        ConfigurationNode chatNode;

        final TypeToken<List<String>> token = new TypeToken<List<String>>() {};

        try {
            chatNode = loader.load();
            chatNode.getNode("banned", "words").setValue(token, actual);
            loader.save(chatNode);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }

        return CommandResult.success();


    }











}
