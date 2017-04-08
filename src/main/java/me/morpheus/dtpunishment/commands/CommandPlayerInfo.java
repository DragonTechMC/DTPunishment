package me.morpheus.dtpunishment.commands;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.utils.Util;

public class CommandPlayerInfo implements CommandExecutor {

    @Inject
    private DataStore dataStore;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<User> player = args.getOne("player");
        if (player.isPresent()) {
            User user = player.get();
            UUID uuid = user.getUniqueId();

            src.sendMessage(Util.getWatermark().append(Text.of("Player : " + user.getName())).build());
            src.sendMessage(Util.getWatermark().append(Text.of("Mute : " + dataStore.getMutepoints(uuid))).build());
            src.sendMessage(Util.getWatermark().append(Text.of("Ban : " + dataStore.getBanpoints(uuid))).build());

        } else {

            if (!(src instanceof Player)) {
                src.sendMessage(Util.getWatermark().append(Text.of("You need to be a player to execute this")).build());
                return CommandResult.empty();
            }

            UUID uuid = ((Player) src).getUniqueId();

            src.sendMessage(Util.getWatermark().append(Text.of("Player : " + src.getName())).build());
            src.sendMessage(Util.getWatermark().append(Text.of("Mute : " + dataStore.getMutepoints(uuid))).build());
            src.sendMessage(Util.getWatermark().append(Text.of("Ban : " + dataStore.getBanpoints(uuid))).build());

        }

        dataStore.finish();
        return CommandResult.success();
    }
}
