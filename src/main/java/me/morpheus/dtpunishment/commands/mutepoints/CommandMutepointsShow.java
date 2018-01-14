package me.morpheus.dtpunishment.commands.mutepoints;

import java.util.UUID;

import me.morpheus.dtpunishment.DTPunishment;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import com.google.inject.Inject;

import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.utils.Util;

public class CommandMutepointsShow implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		User user = args.<User>getOne("player").get();

		UUID uuid = user.getUniqueId();

		src.sendMessage(Util.withWatermark(user.getName(), " has ", DTPunishment.getDataStore().getMutepoints(uuid), " mutepoints"));

		return CommandResult.success();

	}
}
