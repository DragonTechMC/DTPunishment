package me.morpheus.dtpunishment.commands;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.utils.ConfigUtil;
import me.morpheus.dtpunishment.utils.DBUtil;
import me.morpheus.dtpunishment.utils.Util;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class PlayerInfoCommand implements CommandExecutor {

    private DTPunishment main;

    public PlayerInfoCommand(DTPunishment main){
        this.main = main;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (args.getOne("player").isPresent()) {
            Optional<User> player = Util.getUser(args.getOne("player").get().toString());
            if (!player.isPresent()) {
                src.sendMessage(Text.of(args.getOne("player").get().toString() + " never joined your server "));
                return CommandResult.empty();
            }

            if (ConfigUtil.DB_ENABLED) {
                src.sendMessage(Text.of("Player : " + player.get().getName()));
                src.sendMessage(Text.of("Mute : " + DBUtil.getMutepoints(player.get().getName())));
                src.sendMessage(Text.of("Ban : " + DBUtil.getBanpoints(player.get().getName())));
                return CommandResult.success();
            } else {
                ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), player.get().getName());

                src.sendMessage(Text.of("Player : " + player.get().getName()));
                src.sendMessage(Text.of("Mute : " + playerNode.getNode("points", "mutepoints").getInt()));
                src.sendMessage(Text.of("Ban : " + playerNode.getNode("points", "mutepoints").getInt()));
                return CommandResult.success();
            }

        } else {
            if (ConfigUtil.DB_ENABLED) {
                src.sendMessage(Text.of("Player : " + src.getName()));
                src.sendMessage(Text.of("Mute : " + DBUtil.getMutepoints(src.getName())));
                src.sendMessage(Text.of("Ban : " + DBUtil.getBanpoints(src.getName())));
                return CommandResult.success();
            } else {
                ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), src.getName());

                src.sendMessage(Text.of("Player : " + src.getName()));
                src.sendMessage(Text.of("Mute : " + playerNode.getNode("points", "mutepoints").getInt()));
                src.sendMessage(Text.of("Ban : " + playerNode.getNode("points", "mutepoints").getInt()));
                return CommandResult.success();
            }
        }
    }


}
