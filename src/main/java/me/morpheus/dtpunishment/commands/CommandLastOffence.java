package me.morpheus.dtpunishment.commands;

import me.morpheus.dtpunishment.DTPunishment;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.data.ChatOffenceData;
import me.morpheus.dtpunishment.data.ChatOffenceData.OffenceDetail;
import me.morpheus.dtpunishment.utils.Util;

public class CommandLastOffence implements CommandExecutor {

	private ChatOffenceData chatOffenceData = DTPunishment.getOffenceData();

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		User player = args.<User>getOne("player").get();

		OffenceDetail detail = chatOffenceData.getLastOffence(player.getUniqueId());

		if (detail == null) {
			src.sendMessage(Util.withWatermark(TextColors.AQUA,
					"This user has not offended recently or the server has been restarted since the last offence"));
			return CommandResult.empty();
		}

		src.sendMessage(Util.withWatermark(TextColors.AQUA, "Player ", player.getName(), " sent the message '",
				TextColors.RED, detail.getMessage(), TextColors.AQUA, "' which was flagged for ",
				detail.getOffenceType(), " and incurred ", detail.getPointsIncurred(), " points"));

		return CommandResult.success();
	}

}
