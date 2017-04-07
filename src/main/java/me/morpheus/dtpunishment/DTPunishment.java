package me.morpheus.dtpunishment;

import com.google.inject.Inject;
import me.morpheus.dtpunishment.commands.CommandPlayerInfo;
import me.morpheus.dtpunishment.commands.CommandReloadConfig;
import me.morpheus.dtpunishment.commands.CommandWordAdd;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsAdd;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsRemove;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsShow;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsAdd;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsRemove;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsShow;
import me.morpheus.dtpunishment.configuration.ChatConfig;
import me.morpheus.dtpunishment.configuration.ConfigurationManager;
import me.morpheus.dtpunishment.configuration.DTPunishmentConfig;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.data.DatabaseDataStore;
import me.morpheus.dtpunishment.data.FileDataStore;
import me.morpheus.dtpunishment.listeners.PlayerListener;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private DTPunishmentConfig dtpunishmentConfig;
    private ChatConfig chatconfig;
    private DataStore datastore;

    public DTPunishmentConfig getConfig(){
        return dtpunishmentConfig;
    }

    public ChatConfig getChatConfig() {
        return chatconfig;
    }

    public Path getConfigPath() {
        return privateConfigDir;
    }

    public Path getDefaultConfig() {
        return defaultConfig;
    }

    public Logger getLogger() {
        return logger;
    }

    public DataStore getDatastore() {
        if (datastore == null) {
            datastore = (getConfig().database.enabled) ? new DatabaseDataStore(this) : new FileDataStore(this);
        }
        return datastore;
    }

    @Listener
    public void onServerPreInit(GamePreInitializationEvent event) {
        getLogger().info("Enabling DTPunishment...");

        getLogger().info("Config check...");
        ConfigurationManager config = new ConfigurationManager(this);
        config.generateConfig();
        reloadConfiguration();      
        config.init();
    }

    public void reloadConfiguration() {
        try {
            Path potentialFile = Paths.get(getConfigPath() + "\\chat.conf");
            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder().setPath(potentialFile).build();
            getLogger().info("Initializing config...");
            dtpunishmentConfig = defaultConfigLoader.load().getValue(DTPunishmentConfig.TYPE);
            chatconfig = loader.load().getValue(ChatConfig.TYPE);
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }
    	
    }
    
    @Listener
    public void onServerInit(GameInitializationEvent event) {
        getLogger().info("Registering listeners and commands...");
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
                .executor(new CommandBanpointsAdd(this))
                .build();

        CommandSpec removeBanpoints = CommandSpec.builder()
                .permission("dtpunishment.banpoints.remove")
                .description(Text.of("Remove a specified amount of Banpoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(new CommandBanpointsRemove(this))
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
                .executor(new CommandMutepointsAdd(this))
                .build();

        CommandSpec removeMutepoints = CommandSpec.builder()
                .permission("dtpunishment.mutepoints.add")
                .description(Text.of("Add a specified amount of Mutepoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(new CommandMutepointsRemove(this))
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
                .executor(new CommandPlayerInfo(this))
                .build();

        Sponge.getCommandManager().register(this, playerInfo, "pinfo", "playerinfo");

        CommandSpec addWord = CommandSpec.builder()
                .permission("dtpunishment.word.add")
                .description(Text.of("Add a word to the list of banned ones "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("word"))))
                .executor(new CommandWordAdd(this))
                .build();

        Sponge.getCommandManager().register(this, addWord, "addword");
       
        CommandSpec reloadConfig = CommandSpec.builder()
                .permission("dtpunishment.admin.reload")
                .description(Text.of("Reload configuration from disk"))
                .executor(new CommandReloadConfig(this))
                .build();

        CommandSpec adminCmd = CommandSpec.builder()
                .permission("dtpunishment.admin")
                .description(Text.of("Admin commands for DTPunishment"))
                .child(reloadConfig, "reload")
                .build();       
       
        Sponge.getCommandManager().register(this, adminCmd, "dtp", "dtpunish");
    }
}
