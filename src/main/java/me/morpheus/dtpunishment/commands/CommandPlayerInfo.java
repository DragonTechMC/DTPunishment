package me.morpheus.dtpunishment.commands;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.utils.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

public class CommandPlayerInfo implements CommandExecutor {

    private final DTPunishment main;

    public CommandPlayerInfo(DTPunishment main){
        this.main = main;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<String> player = args.getOne("player");
        if (player.isPresent()) {
            Optional<User> user = Util.getUser(player.get());
            if (!user.isPresent()) {
                src.sendMessage(Util.getWatermark().append(Text.of(player.get() + " never joined your server ")).build());
                return CommandResult.empty();
            }

            UUID uuid = user.get().getUniqueId();

            src.sendMessage(Util.getWatermark().append(Text.of("Player : " + user.get().getName())).build());
            src.sendMessage(Util.getWatermark().append(Text.of("Mute : " + main.getDatastore().getMutepoints(uuid))).build());
            src.sendMessage(Util.getWatermark().append(Text.of("Ban : " + main.getDatastore().getBanpoints(uuid))).build());

        } else {

            if (!(src instanceof Player)) {
                src.sendMessage(Util.getWatermark().append(Text.of("You need to be a player to execute this")).build());
                return CommandResult.empty();
            }

            UUID uuid = ((Player) src).getUniqueId();

            src.sendMessage(Util.getWatermark().append(Text.of("Player : " + src.getName())).build());
            src.sendMessage(Util.getWatermark().append(Text.of("Mute : " + main.getDatastore().getMutepoints(uuid))).build());
            src.sendMessage(Util.getWatermark().append(Text.of("Ban : " + main.getDatastore().getBanpoints(uuid))).build());

        }

        main.getDatastore().finish();
        return CommandResult.success();
    }
}
