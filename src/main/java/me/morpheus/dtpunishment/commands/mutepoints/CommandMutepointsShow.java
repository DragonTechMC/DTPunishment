package me.morpheus.dtpunishment.commands.mutepoints;

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

public class CommandMutepointsShow implements CommandExecutor {

    private DTPunishment main;

    public CommandMutepointsShow(DTPunishment main){
        this.main = main;
    }



    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {


        Optional<User> user = Util.getUser(args.<String>getOne("player").get());
        if (!user.isPresent()) {
            src.sendMessage(Text.of(args.<String>getOne("player").get() + " never joined your server "));
            return CommandResult.empty();
        }
        String name = user.get().getName();
        if (ConfigUtil.DB_ENABLED) {
            src.sendMessage(Text.of(name + " has " + DBUtil.getMutepoints(name) + " mutepoints"));
            return CommandResult.success();
        } else {
            ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), name);
            int amount = playerNode.getNode("points", "mutepoints").getInt();
            src.sendMessage(Text.of(name + " has " + amount + " mutepoints"));
            return CommandResult.success();
        }

    }
}
