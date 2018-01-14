package me.morpheus.dtpunishment.penalty;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import me.morpheus.dtpunishment.DTPunishment;
import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.morpheus.dtpunishment.configuration.MainConfig;
import me.morpheus.dtpunishment.configuration.Punishment;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.utils.Util;

public class MutepointsPunishment {

	public static void check(UUID uuid, int amount) {

		User user = Util.getUser(uuid);

		Punishment punishment = DTPunishment.getConfig().punishments.getApplicableMutepointsPunishment(amount);

		if (punishment == null) {
			DTPunishment.getLogger().info(String.format("Could not find punishment for %d mutepoints", amount));
			return;
		}

		if (punishment.banpoints > 0) {
			DTPunishment.getDataStore().addBanpoints(uuid, punishment.banpoints);
			BanpointsPunishment.check(uuid, DTPunishment.getDataStore().getBanpoints(uuid));
		}

		Instant expiration = Instant.now().plus(punishment.length.duration);

		DTPunishment.getDataStore().mute(uuid, expiration);

		Text message = Util.withWatermark(TextColors.RED,
				String.format(DTPunishment.getMessages().PLAYER_MUTED_MESSAGE_STAFF, user.getName(),
						Util.durationToString(punishment.length.duration), punishment.threshold));

        Sponge.getServer().getConsole().sendMessage(message);

		for (Player pl : Sponge.getServer().getOnlinePlayers()) {
			pl.sendMessage(message);
		}

		if (user.isOnline()) {
			user.getPlayer().get()
					.sendMessage(Util.withWatermark(TextColors.RED,
							String.format(DTPunishment.getMessages().PLAYER_EXCEEDED_POINTS,
									Util.durationToString(punishment.length.duration), punishment.threshold)));
		}
	}
}
