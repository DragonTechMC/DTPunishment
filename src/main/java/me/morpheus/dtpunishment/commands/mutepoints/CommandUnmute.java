package me.morpheus.dtpunishment.commands.mutepoints;

import java.util.Optional;

import org.spongepowered.api.Server;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.utils.Util;

public class CommandUnmute implements CommandExecutor {

	private DataStore dataStore;

	private Server server;

	@Inject
	public CommandUnmute(DataStore dataStore, Server server) {
		this.dataStore = dataStore;
		this.server = server;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		User user = args.<User>getOne("player").get();

		dataStore.unmute(user.getUniqueId());

		// If player is online, notify them
		Optional<Player> player = user.getPlayer();

		Text notification = Util.withWatermark(TextColors.AQUA,
				String.format("%s has been unmuted by %s", user.getName(), src.getName()));

		if (src instanceof ConsoleSource)
			src.sendMessage(notification);

		if (player.isPresent()) {
			player.get().sendMessage(notification);
		}

		for (Player p : server.getOnlinePlayers()) {
			if (p.hasPermission("dtpunishment.staff.notify") || p.getPlayer().get() == src) {
				p.sendMessage(notification);
			}
		}

		return CommandResult.success();
	}

}
