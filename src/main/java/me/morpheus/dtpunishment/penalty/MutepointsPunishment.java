package me.morpheus.dtpunishment.penalty;

import me.morpheus.dtpunishment.configuration.MainConfig;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.utils.Util;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class MutepointsPunishment {

	@Inject 
	private MainConfig mainConfig;

	@Inject
	private DataStore dataStore;

	@Inject
	private BanpointsPunishment banPunish;

	@Inject
	private Logger logger;

	public void check(UUID uuid, int amount) {

		if (amount < 5) return;

		int rounded = (amount < 10) ? 5 : amount/10 * 10;

		String punishment = mainConfig.punishment.mutepoints.get(rounded + " mutepoints");

		if(punishment == null) {
			logger.info(String.format("Could not find punishment for %d mutepoints", rounded));
			return;
		}

		if (punishment.substring(0, 1).equalsIgnoreCase("+")) {
			int bp = Integer.parseInt(punishment.substring(1, punishment.length() - 2));

			dataStore.addBanpoints(uuid, bp);
			banPunish.check(uuid, bp);
		} else {
			int minutes = Integer.parseInt(punishment.substring(0, punishment.length() - 1));
			Instant expiration = Instant.now().plus(Duration.ofMinutes(minutes));

			dataStore.mute(uuid, expiration);

			Optional<User> userOpt = Util.getUser(uuid);

			if(userOpt.isPresent()) {
				for (Player pl : Sponge.getServer().getOnlinePlayers()) {
					Text message = Util.getWatermark().append(
							Text.builder(userOpt.get().getName() + " has been muted for " + minutes + " minutes for exceeding "
									+ rounded + " mutepoint(s)").color(TextColors.RED).build()).build();
					pl.sendMessage(message);
				}
			}
		}

	}


}
