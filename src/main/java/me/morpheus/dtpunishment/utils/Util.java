package me.morpheus.dtpunishment.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class Util {

	public static Optional<User> getUser(UUID uuid) {
		Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
		return userStorage.get().get(uuid);
	}

	public static Text withWatermark(Object... text) {
		return Text.builder("[DTP] ").color(TextColors.GOLD).append(Text.of(text)).build();
	}

	public static String durationToString(Duration duration) {

		// Get days
		long days = duration.toDays();
		if (days > 0)
			duration = duration.minusDays(days);

		long hours = duration.toHours();
		if (hours > 0)
			duration = duration.minusHours(hours);

		long minutes = duration.toMinutes();
		if (minutes > 0)
			duration = duration.minusMinutes(minutes);

		long seconds = duration.toMillis() * 1000;

		String result = "";

		if (days > 0)
			result += days + " days";

		if (hours > 0)
			result += (result.length() > 0 ? ", " : "") + hours + " hours";

		if (minutes > 0)
			result += (result.length() > 0 ? ", " : "") + minutes + " minutes";

		if (seconds > 0)
			result += (result.length() > 0 ? ", " : "") + seconds + " seconds";

		return result;
	}

	public static String instantToString(Instant instant) {
		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(instant.toEpochMilli());
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		return dateFormat.format(cal.getTime());
	}
}
