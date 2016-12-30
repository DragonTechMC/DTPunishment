package me.morpheus.dtpunishment;

import com.google.inject.Inject;
import me.morpheus.dtpunishment.command.BanpointsCommand;
import me.morpheus.dtpunishment.command.MutepointsCommand;
import me.morpheus.dtpunishment.command.PlayerInfoCommand;
import me.morpheus.dtpunishment.listener.PlayerListener;
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

import java.nio.file.Path;
import java.sql.SQLException;

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
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    public Path getConfigPath() {
        return privateConfigDir;
    }

    public Path getDefaultConfig(){
        return defaultConfig;
    }

    public Logger getLogger() {
        return logger;
    }



    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        getLogger().info("Hello world!");
        getLogger().info("If you don't refactor me, I'm gonna kill myself");
        ConfigurationManager config = new ConfigurationManager(this);
        config.generateConfig();
        try {
            config.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Sponge.getEventManager().registerListeners(this, new PlayerListener(this));
        registerCommand();



    }












    public void registerCommand(){

        CommandSpec showBanpoints = CommandSpec.builder()
                .permission("dtpunishment.banpoints.show")
                .description(Text.of("Shows how many ban points the specified player has"))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .executor(new BanpointsCommand(this))
                .build();


        CommandSpec addBanpoints = CommandSpec.builder()
                .permission("dtpunishment.banpoints.add")
                .description(Text.of("Adds a specified amount of ban points to the specified player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(new BanpointsCommand(this))
                .build();

        CommandSpec banpoints = CommandSpec.builder()
                .permission("dtpunishment.banpoints")
                .description(Text.of("Shows the Ban points help menu"))
                .arguments(GenericArguments.none())
                .executor(new BanpointsCommand(this))
                .child(showBanpoints, "show")
                .child(addBanpoints, "add")
                .build();

        Sponge.getCommandManager().register(this, banpoints, "banpoints");



        CommandSpec showMutepoints = CommandSpec.builder()
                .permission("dtpunishment.mutepoints.show")
                .description(Text.of("Shows how many mute points the specified player has"))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .executor(new MutepointsCommand(this))
                .build();


        CommandSpec addMutepoints = CommandSpec.builder()
                .permission("dtpunishment.mutepoints.add")
                .description(Text.of("Adds a specified amount of mute points to the specified player  "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(new MutepointsCommand(this))
                .build();

        CommandSpec mutepoints = CommandSpec.builder()
                .permission("dtpunishment.mutepoints")
                .description(Text.of("Shows the Mute points help menu"))
                .arguments(GenericArguments.none())
                .executor(new MutepointsCommand(this))
                .child(showMutepoints, "show")
                .child(addMutepoints, "add")
                .build();

        Sponge.getCommandManager().register(this, mutepoints, "mutepoints");


        CommandSpec playerInfoShow = CommandSpec.builder()
                .permission("dtpunishment.playerinfo.others")
                .description(Text.of("Shows a player's info (Only shows others if moderator)"))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .executor(new PlayerInfoCommand(this))
                .build();

        CommandSpec playerInfo = CommandSpec.builder()
                .permission("dtpunishment.playerinfo")
                .description(Text.of("Shows your info "))
                .arguments(GenericArguments.none())
                .executor(new PlayerInfoCommand(this))
                .child(playerInfoShow, "show")
                .build();

        Sponge.getCommandManager().register(this, playerInfo, "pinfo", "playerinfo");


    }






}
