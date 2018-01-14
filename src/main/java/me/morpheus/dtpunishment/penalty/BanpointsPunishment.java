package me.morpheus.dtpunishment.penalty;

import java.time.Instant;
import java.util.UUID;

import me.morpheus.dtpunishment.DTPunishment;
import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.morpheus.dtpunishment.configuration.MainConfig;
import me.morpheus.dtpunishment.configuration.Punishment;
import me.morpheus.dtpunishment.utils.Util;

public class BanpointsPunishment {

	public static void check(UUID uuid, int amount) {

		Punishment punishment = DTPunishment.getConfig().punishments.getApplicableBanpointsPunishment(amount);

		if (punishment == null) {
			DTPunishment.getLogger().info(String.format("No punishment exists for %d banpoints", amount));
			return;
		}

		BanService service = Sponge.getServiceManager().provide(BanService.class).get();

		Instant expiration = Instant.now().plus(punishment.length.duration);

		String durationText = Util.durationToString(punishment.length.duration);

		User user = Util.getUser(uuid);

		Ban ban = Ban.builder().type(BanTypes.PROFILE).profile(user.getProfile()).expirationDate(expiration)
				.reason(Util.withWatermark(TextColors.AQUA, TextStyles.BOLD,
						String.format(DTPunishment.getMessages().PLAYER_BANNED_EXCEEDED_POINTS, durationText,
								punishment.threshold)))
				.build();
		service.addBan(ban);

		Text message = Util.withWatermark(TextColors.RED,
				String.format(DTPunishment.getMessages().PLAYER_BANNED_EXCEEDED_POINTS_STAFF, user.getName(), durationText,
						punishment.threshold));

		Sponge.getServer().getConsole().sendMessage(message);

		for (Player pl : Sponge.getServer().getOnlinePlayers()) {
			pl.sendMessage(message);
		}

		if (user.isOnline()) {
			user.getPlayer().get().kick(Util.withWatermark(TextColors.AQUA, TextStyles.BOLD, String.format(
					DTPunishment.getMessages().PLAYER_BANNED_EXCEEDED_POINTS, durationText, punishment.threshold)));
		}

	}

}
