package me.morpheus.dtpunishment.commands;

import me.morpheus.dtpunishment.DTPunishment;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandWordAdd implements CommandExecutor {

    private DTPunishment main;

    public CommandWordAdd(DTPunishment main){
        this.main = main;
    }


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Path chatData = Paths.get(main.getConfigPath() + "/chat.conf");

        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(chatData).build();
        ConfigurationNode chatNode = null;
        try {
            chatNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String str = chatNode.getNode("banned", "words").getString();
        String word = args.<String>getOne("word").get();


        chatNode.getNode("banned", "words").setValue(str+","+word);
        try {
            loader.save(chatNode);
        } catch (IOException e) {
            e.printStackTrace();
            return CommandResult.empty();
        }
        return CommandResult.success();


    }











}
