package me.morpheus.dtpunishment;

import com.google.inject.Inject;
import me.morpheus.dtpunishment.commands.CommandWordAdd;
import me.morpheus.dtpunishment.commands.PlayerInfoCommand;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsEdit;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsShow;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsEdit;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsShow;
import me.morpheus.dtpunishment.listeners.PlayerListener;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "dtpunishment",
        name = "DTPunishment"
)
public class DTPunishment {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> defaultConfigLoader;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    public Path getConfigPath() {
        return privateConfigDir;
    }

    Path getDefaultConfig() {
        return defaultConfig;
    }

    public ConfigurationLoader<CommentedConfigurationNode> getDefaultConfigLoader() {
        return defaultConfigLoader;
    }

    public Logger getLogger() {
        return logger;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        //getLogger().info("If you don't refactor me, I'm gonna kill myself");
        //getLogger().info("Ok, now it's better");
        getLogger().info("Enabling DTPunishment...");
        ConfigurationManager config = new ConfigurationManager(this);
        config.generateConfig();
        try {
            config.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Sponge.getEventManager().registerListeners(this, new PlayerListener(this));
        registerCommand();
    }





    private void registerCommand() {

        CommandSpec showBanpoints = CommandSpec.builder()
                .permission("dtpunishment.banpoints.show")
                .description(Text.of("Show how many Banpoints the specified player has "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .executor(new CommandBanpointsShow(this))
                .build();

        CommandSpec addBanpoints = CommandSpec.builder()
                .permission("dtpunishment.banpoints.add")
                .description(Text.of("Add a specified amount of Banpoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(new CommandBanpointsEdit(this, "add"))
                .build();

        CommandSpec removeBanpoints = CommandSpec.builder()
                .permission("dtpunishment.banpoints.remove")
                .description(Text.of("Remove a specified amount of Banpoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(new CommandBanpointsEdit(this, "remove"))
                .build();

        CommandSpec banpoints = CommandSpec.builder()
                .permission("dtpunishment.banpoints")
                .description(Text.of("Show the Banpoints help menu"))
                .arguments(GenericArguments.none())
                .child(showBanpoints, "show")
                .child(addBanpoints, "add")
                .child(removeBanpoints, "remove")
                .build();

        Sponge.getCommandManager().register(this, banpoints, "banpoints", "bp");



        CommandSpec showMutepoints = CommandSpec.builder()
                .permission("dtpunishment.mutepoints.show")
                .description(Text.of("Show how many Mutepoints the specified player has "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .executor(new CommandMutepointsShow(this))
                .build();

        CommandSpec addMutepoints = CommandSpec.builder()
                .permission("dtpunishment.mutepoints.add")
                .description(Text.of("Add a specified amount of Mutepoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(new CommandMutepointsEdit(this, "add"))
                .build();

        CommandSpec removeMutepoints = CommandSpec.builder()
                .permission("dtpunishment.mutepoints.add")
                .description(Text.of("Add a specified amount of Mutepoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(new CommandMutepointsEdit(this, "remove"))
                .build();

        CommandSpec mutepoints = CommandSpec.builder()
                .permission("dtpunishment.mutepoints")
                .description(Text.of("Show the Mutepoints help menu"))
                .arguments(GenericArguments.none())
                .child(showMutepoints, "show")
                .child(addMutepoints, "add")
                .child(removeMutepoints, "remove")
                .build();

        Sponge.getCommandManager().register(this, mutepoints, "mutepoints", "mp");


        CommandSpec playerInfo = CommandSpec.builder()
                .permission("dtpunishment.playerinfo")
                .description(Text.of("Show your info "))
                .arguments(GenericArguments.onlyOne(GenericArguments.optionalWeak(GenericArguments.requiringPermission(
                                GenericArguments.string(Text.of("player")), "dtpunishment.playerinfo.others"))))
                .executor(new PlayerInfoCommand(this))
                .build();

        Sponge.getCommandManager().register(this, playerInfo, "pinfo", "playerinfo");

        CommandSpec addWord = CommandSpec.builder()
                .permission("dtpunishment.word.add")
                .description(Text.of("Add a word to the list of banned ones "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("word"))))
                .executor(new CommandWordAdd(this))
                .build();

        Sponge.getCommandManager().register(this, addWord, "addword");

    }






}
