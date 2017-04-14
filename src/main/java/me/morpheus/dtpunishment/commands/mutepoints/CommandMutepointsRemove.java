package me.morpheus.dtpunishment.commands.mutepoints;

import java.util.UUID;

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

public class CommandMutepointsRemove implements CommandExecutor {

	private DataStore dataStore;

	private Server server;

	@Inject
	public CommandMutepointsRemove(DataStore dataStore, Server server) {
		this.dataStore = dataStore;
		this.server = server;
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		User user = args.<User>getOne("player").get();
		UUID uuid = user.getUniqueId();
		String name = user.getName();
		int actual = dataStore.getMutepoints(uuid);
		int amount = args.<Integer>getOne("amount").get();

		if (actual - amount < 0)
			amount = actual;
		int total = actual - amount;

		dataStore.removeMutepoints(uuid, amount);

		if (user.isOnline()) {
			user.getPlayer().get().sendMessage(Util.withWatermark(TextColors.AQUA,
					String.format("%d mutepoints have been removed; you now have %d", amount, total)));
		}

		Text adminMessage = Util.withWatermark(TextColors.AQUA, String.format(
				"%s has removed %d mutepoint(s) from %s; they now have %d", src.getName(), amount, name, total));

		if (src instanceof ConsoleSource)
			src.sendMessage(adminMessage);

		for (Player p : server.getOnlinePlayers()) {
			if (p.hasPermission("dtpunishment.staff.notify") || p == src) {
				p.sendMessage(adminMessage);
			}
		}

		return CommandResult.success();

	}
}
