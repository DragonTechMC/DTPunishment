package me.morpheus.dtpunishment;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;
import com.google.inject.Injector;

import me.morpheus.dtpunishment.commands.CommandPlayerInfo;
import me.morpheus.dtpunishment.commands.CommandReloadConfig;
import me.morpheus.dtpunishment.commands.CommandWordAdd;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsAdd;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsRemove;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsShow;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsAdd;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsRemove;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsShow;
import me.morpheus.dtpunishment.commands.mutepoints.CommandUnmute;
import me.morpheus.dtpunishment.configuration.ConfigurationManager;
import me.morpheus.dtpunishment.configuration.MainConfig;
import me.morpheus.dtpunishment.listeners.PlayerListener;

@Plugin(id = "dtpunishment", name = "DTPunishment")
public class DTPunishment {

    @Inject
    private Logger logger;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private Injector injector;

    private Injector childInjector;

    @Inject
    private MainConfig config;

    @Listener
    public void onServerPreInit(GamePreInitializationEvent event) {
        logger.info("Enabling DTPunishment...");

        configurationManager.intialise();

        // Create the child injector for the plugin
        childInjector = injector.createChildInjector(new DTPunishmentModule(config));
    }

    @Listener
    public void onServerInit(GameInitializationEvent event) {
        logger.info("Registering listeners and commands...");
        Sponge.getEventManager().registerListeners(this, childInjector.getInstance(PlayerListener.class));
        registerCommands();
    }

    private void registerCommands() {

        // We will have to use the injector to get the commands from the
        // container
        // since we couldn't setup the child injector before the plugin was
        // instantiated
        // alternatively we could create an init class that we resolve from the
        // container
        // but this is quick and dirty and we don't need more complexity yet
        CommandSpec showBanpoints = CommandSpec.builder().permission("dtpunishment.banpoints.show")
                .description(Text.of("Show how many Banpoints the specified player has "))
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))))
                .executor(childInjector.getInstance(CommandBanpointsShow.class)).build();

        CommandSpec addBanpoints = CommandSpec.builder().permission("dtpunishment.banpoints.add")
                .description(Text.of("Add a specified amount of Banpoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(childInjector.getInstance(CommandBanpointsAdd.class)).build();

        CommandSpec removeBanpoints = CommandSpec.builder().permission("dtpunishment.banpoints.remove")
                .description(Text.of("Remove a specified amount of Banpoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(childInjector.getInstance(CommandBanpointsRemove.class)).build();

        CommandSpec banpoints = CommandSpec.builder().permission("dtpunishment.banpoints")
                .description(Text.of("Show the Banpoints help menu")).arguments(GenericArguments.none())
                .child(showBanpoints, "show").child(addBanpoints, "add").child(removeBanpoints, "remove").build();

        Sponge.getCommandManager().register(this, banpoints, "banpoints", "bp");

        CommandSpec showMutepoints = CommandSpec.builder().permission("dtpunishment.mutepoints.show")
                .description(Text.of("Show how many Mutepoints the specified player has "))
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))))
                .executor(childInjector.getInstance(CommandMutepointsShow.class)).build();

        CommandSpec addMutepoints = CommandSpec.builder().permission("dtpunishment.mutepoints.add")
                .description(Text.of("Add a specified amount of Mutepoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(childInjector.getInstance(CommandMutepointsAdd.class)).build();

        CommandSpec removeMutepoints = CommandSpec.builder().permission("dtpunishment.mutepoints.add")
                .description(Text.of("Add a specified amount of Mutepoints to a player "))
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))),
                        GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
                .executor(childInjector.getInstance(CommandMutepointsRemove.class)).build();

        CommandSpec mutepoints = CommandSpec.builder().permission("dtpunishment.mutepoints")
                .description(Text.of("Show the Mutepoints help menu")).arguments(GenericArguments.none())
                .child(showMutepoints, "show").child(addMutepoints, "add").child(removeMutepoints, "remove").build();

        Sponge.getCommandManager().register(this, mutepoints, "mutepoints", "mp");

        CommandSpec playerInfo = CommandSpec.builder().permission("dtpunishment.playerinfo")
                .description(Text.of("Show your info "))
                .arguments(GenericArguments.onlyOne(GenericArguments.optionalWeak(GenericArguments.requiringPermission(
                        GenericArguments.user(Text.of("player")), "dtpunishment.playerinfo.others"))))
                .executor(childInjector.getInstance(CommandPlayerInfo.class)).build();

        Sponge.getCommandManager().register(this, playerInfo, "pinfo", "playerinfo");

        CommandSpec addWord = CommandSpec.builder().permission("dtpunishment.word.add")
                .description(Text.of("Add a word to the list of banned ones "))
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("word"))))
                .executor(childInjector.getInstance(CommandWordAdd.class)).build();

        Sponge.getCommandManager().register(this, addWord, "addword");

        CommandSpec unmute = CommandSpec.builder().permission("dtpunishment.mutepoints.add")
                .description(Text.of("Unmute a player immediately (removing all mutepoints)"))
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))))
                .executor(childInjector.getInstance(CommandUnmute.class)).build();

        CommandSpec reloadConfig = CommandSpec.builder().permission("dtpunishment.admin.reload")
                .description(Text.of("Reload configuration from disk"))
                .executor(childInjector.getInstance(CommandReloadConfig.class)).build();

        CommandSpec adminCmd = CommandSpec.builder().permission("dtpunishment.admin")
                .description(Text.of("Admin commands for DTPunishment")).child(reloadConfig, "reload")
                .child(unmute, "unmute").build();

        Sponge.getCommandManager().register(this, adminCmd, "dtp", "dtpunish");
    }
}
