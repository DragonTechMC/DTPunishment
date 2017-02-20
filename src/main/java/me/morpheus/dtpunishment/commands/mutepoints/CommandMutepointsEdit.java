package me.morpheus.dtpunishment.commands.mutepoints;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.PunishmentManager;
import me.morpheus.dtpunishment.utils.ConfigUtil;
import me.morpheus.dtpunishment.utils.DBUtil;
import me.morpheus.dtpunishment.utils.Util;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class CommandMutepointsEdit implements CommandExecutor {

    private DTPunishment main;
    private String action;

    public CommandMutepointsEdit(DTPunishment main, String action){
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
                DBUtil.addMutepoints(name, -amount);
            } else if (action.equalsIgnoreCase("add")) {
                DBUtil.addMutepoints(name, amount);
            }
            punishment.checkPenalty(name, "mutepoints", DBUtil.getMutepoints(name));
            return CommandResult.success();
        } else {
            ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), name);
            int actual = playerNode.getNode("points", "mutepoints").getInt();
            if (action.equalsIgnoreCase("remove")) {
                playerNode.getNode("points", "mutepoints").setValue(actual-amount);
                user.get().getPlayer().get().sendMessage(Text.of(TextColors.AQUA, amount + " mutepoints have been removed; you now have " + (actual-amount)));
                src.sendMessage(Text.of(TextColors.AQUA, "You have removed " + amount + " mutepoints from " + name + "; they now have " + (actual-amount)));
                for (Player p : Sponge.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("dtpunishment.staff.notify")) {
                        Text message = Text.builder("[DTP] ").color(TextColors.GOLD).append(
                                Text.builder(src.getName() + " has removed " + amount + " mutepoint(s) from "
                                        + name +  "; they now have " + (actual-amount)).color(TextColors.AQUA).build()).build();
                        p.sendMessage(message);
                    }
                }
            } else if (action.equalsIgnoreCase("add")) {
                playerNode.getNode("points", "mutepoints").setValue(actual+amount);
                user.get().getPlayer().get().sendMessage(Text.of(TextColors.RED, amount + " mutepoints have been added; you now have " + (actual+amount)));
                src.sendMessage(Text.of(TextColors.RED, "You have added " + amount + " mutepoints to " + name + "; they now have " + (actual+amount)));
                for (Player p : Sponge.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("dtpunishment.staff.notify")) {
                        Text message = Text.builder("[DTP] ").color(TextColors.GOLD).append(
                                Text.builder(src.getName() + " has added " + amount + " mutepoint(s) to "
                                        + name +  "; they now have " + (actual+amount)).color(TextColors.RED).build()).build();
                        p.sendMessage(message);
                    }
                }
            }
            ConfigUtil.save(main.getConfigPath(), name, playerNode);
            punishment.checkPenalty(name, "mutepoints", playerNode.getNode("points", "mutepoints").getInt());
            return CommandResult.success();
        }

    }
}
