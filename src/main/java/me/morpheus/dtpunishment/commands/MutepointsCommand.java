package me.morpheus.dtpunishment.commands;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.PunishmentManager;
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

public class MutepointsCommand implements CommandExecutor {
    private DTPunishment main;

    public MutepointsCommand(DTPunishment main){
        this.main = main;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (args.getOne("target").isPresent() || args.getOne("player").isPresent()) {
            Optional<User> player;
            if (args.getOne("target").isPresent()) {
                player = Util.getUser(args.getOne("target").get().toString());
                if (!player.isPresent()) {
                    src.sendMessage(Text.of(args.getOne("target").get().toString() + " never joined your server "));
                    return CommandResult.empty();
                }
            } else {
                player = Util.getUser(args.getOne("player").get().toString());
                if (!player.isPresent()) {
                    src.sendMessage(Text.of(args.getOne("player").get().toString() + " never joined your server "));
                    return CommandResult.empty();
                }
            }

            ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), player.get().getName());

            if (args.getOne("target").isPresent() && args.<Integer>getOne("amount").isPresent()) {
                PunishmentManager punishment = new PunishmentManager(main);
                int added = args.<Integer>getOne("amount").get();

                if (ConfigUtil.DB_ENABLED) {
                    DBUtil.addMutepoints(player.get().getName(), added);
                    punishment.checkPenalty(player.get().getName(), "mutepoints", DBUtil.getMutepoints(player.get().getName()));
                    return CommandResult.success();
                } else {
                    int actual = playerNode.getNode("points", "mutepoints").getInt();
                    playerNode.getNode("points", "mutepoints").setValue(added + actual);
                    ConfigUtil.save(main.getConfigPath(), player.get().getName(), playerNode);
                    punishment.checkPenalty(player.get().getName(), "mutepoints", playerNode.getNode("points", "mutepoints").getInt());
                    return CommandResult.success();
                }
            } else if (args.getOne("player").isPresent()) {
                if (ConfigUtil.DB_ENABLED) {
                    src.sendMessage(Text.of(player.get().getName() + " has " + DBUtil.getMutepoints(player.get().getName()) + " mutepoints"));
                    return CommandResult.success();
                } else {
                    int amount = playerNode.getNode("points", "mutepoints").getInt();
                    src.sendMessage(Text.of(player.get().getName() + " has " + amount + " mutepoints"));
                    return CommandResult.success();
                }
            }
        } else {
            src.sendMessage(Text.of("Show the Mutepoints help menu"));
            return CommandResult.success();
        }
        return CommandResult.empty();
    }
}
