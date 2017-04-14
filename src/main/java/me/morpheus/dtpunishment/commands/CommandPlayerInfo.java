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
import com.google.inject.Inject;

import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.utils.Util;

public class CommandPlayerInfo implements CommandExecutor {

	private DataStore dataStore;

	@Inject
	private CommandPlayerInfo(DataStore dataStore) {
		this.dataStore = dataStore;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<User> player = args.getOne("player");
		if (player.isPresent()) {
			User user = player.get();
			UUID uuid = user.getUniqueId();

			src.sendMessage(Util.withWatermark("Player : " + user.getName()));
			src.sendMessage(Util.withWatermark("Mute : " + dataStore.getMutepoints(uuid)));
			src.sendMessage(Util.withWatermark("Ban : " + dataStore.getBanpoints(uuid)));

		} else {

			if (!(src instanceof Player)) {
				src.sendMessage(Util.withWatermark("You need to be a player to execute this"));
				return CommandResult.empty();
			}

			UUID uuid = ((Player) src).getUniqueId();

			src.sendMessage(Util.withWatermark("Player : " + src.getName()));
			src.sendMessage(Util.withWatermark("Mute : " + dataStore.getMutepoints(uuid)));
			src.sendMessage(Util.withWatermark("Ban : " + dataStore.getBanpoints(uuid)));

		}

		return CommandResult.success();
	}
}
