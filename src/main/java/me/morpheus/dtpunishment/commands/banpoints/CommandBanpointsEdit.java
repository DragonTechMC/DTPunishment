package me.morpheus.dtpunishment.commands.banpoints;

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

public class CommandBanpointsEdit implements CommandExecutor {

    private DTPunishment main;
    private String action;

    public CommandBanpointsEdit(DTPunishment main, String action){
        this.main = main;
        this.action = action;
    }



    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Optional<User> user = Util.getUser(args.<String>getOne("player").get());
        if (!user.isPresent()) {
            src.sendMessage(Text.of(args.<String>getOne("player").get() + " never joined your server "));
            return CommandResult.empty();
        }
        String name = user.get().getName();
        PunishmentManager punishment = new PunishmentManager(main);
        int amount = args.<Integer>getOne("amount").get();

        if (ConfigUtil.DB_ENABLED) {
            if (action.equalsIgnoreCase("remove")) {
                DBUtil.addBanpoints(name, -amount);
            } else if (action.equalsIgnoreCase("add")) {
                DBUtil.addBanpoints(name, amount);
            }
            punishment.checkPenalty(name, "banpoints", DBUtil.getBanpoints(name));
            return CommandResult.success();
        } else {
            ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), name);
            int actual = playerNode.getNode("points", "banpoints").getInt();
            if (action.equalsIgnoreCase("remove")) {
                playerNode.getNode("points", "banpoints").setValue(actual-amount);
            } else if (action.equalsIgnoreCase("add")) {
                playerNode.getNode("points", "banpoints").setValue(actual+amount);
            }
            ConfigUtil.save(main.getConfigPath(), name, playerNode);
            punishment.checkPenalty(name, "banpoints", playerNode.getNode("points", "banpoints").getInt());
            return CommandResult.success();
        }

    }
}
