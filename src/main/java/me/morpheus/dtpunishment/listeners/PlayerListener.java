package me.morpheus.dtpunishment.listeners;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import me.morpheus.dtpunishment.DTPunishment;
import org.slf4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.WordChecker;
import me.morpheus.dtpunishment.configuration.ChatConfig;
import me.morpheus.dtpunishment.data.ChatOffenceData;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.penalty.MutepointsPunishment;
import me.morpheus.dtpunishment.utils.Util;
import org.spongepowered.api.text.serializer.TextSerializers;

public class PlayerListener {

	private Logger logger = DTPunishment.getLogger();

	private DataStore dataStore = DTPunishment.getDataStore();

	private WordChecker wordChecker = DTPunishment.getWordChecker();

	private ChatConfig chatConfig = DTPunishment.getChatConfig();

	private ChatOffenceData chatOffenceData = DTPunishment.getOffenceData();

	@Listener
	public void onPlayerPreJoin(ClientConnectionEvent.Login event) {
		User user = event.getTargetUser();

		if (wordChecker.containsBannedWords(user.getName())) {

			String word = wordChecker.getBannedWord(user.getName());

			event.setMessage(Util.withWatermark(TextColors.AQUA, TextStyles.BOLD,
					String.format(
							DTPunishment.getMessages().USERNAME_HAS_BANNED_WORDS,
							word)));
			event.setCancelled(true);
		}
	}

	@Listener
	public void onPlayerJoin(ClientConnectionEvent.Join event) {

		// Check the player name against the banned words list
		Player player = event.getTargetEntity();
		UUID uuid = player.getUniqueId();

		if (!dataStore.userExists(uuid)) {
			logger.info(player.getName() + " not found, creating player data...");
			dataStore.createUser(uuid);
		} else {
			LocalDate now = LocalDate.now();
			if (now.isAfter(dataStore.getMutepointsUpdatedAt(uuid).plusMonths(1))) {
				int actual = dataStore.getMutepoints(uuid);
				int amount = Math.min(5, actual);
				dataStore.removeMutepoints(uuid, amount);
				dataStore.addMutepoints(uuid, 0);
			}
			if (now.isAfter(dataStore.getBanpointsUpdatedAt(uuid).plusMonths(1))) {
				int actual = dataStore.getBanpoints(uuid);

				if (actual > 0) {
					int amount = 1;
					dataStore.removeBanpoints(uuid, amount);
					dataStore.addBanpoints(uuid, 0);
				}
			}
		}
	}

	@Listener
	public void onPlayerChat(MessageChannelEvent.Chat event, @Root Player player) {

		String message = event.getRawMessage().toPlain();
		UUID uuid = player.getUniqueId();
		int mutePointsIncurred = 0;

		if (dataStore.isMuted(uuid)) {
			Instant expiration = dataStore.getExpiration(uuid);

			if (Instant.now().isAfter(expiration)) {
				dataStore.unmute(uuid);
			} else {
				logger.info("[Message cancelled] - " + event.getMessage().toPlain());
				player.sendMessage(Util.withWatermark(TextColors.RED,
						String.format(
								DTPunishment.getMessages().PLAYER_MUTED_MESSAGE,
								Util.instantToString(expiration), dataStore.getMutepoints(uuid))));

				event.setMessageCancelled(true);
				return;
			}
		}

		boolean containsPlayer = false;
		for (Player p : Sponge.getServer().getOnlinePlayers()) {
			for (String s : message.split(" ")) {
				if (p.getName().toLowerCase().equals(s.toLowerCase())) {
					containsPlayer = true;
				}
			}
		}

		if (wordChecker.isSpam(message, uuid)) {
			int points = chatConfig.spam.mutepoints;
			mutePointsIncurred += points;
			dataStore.addMutepoints(uuid, points);

			player.sendMessage(Util.withWatermark(TextColors.RED,
					String.format(
					DTPunishment.getMessages().PLAYER_SPAMMING_MESSAGE,
                            points, dataStore.getMutepoints(uuid))
                    ));

			logger.info("[Message cancelled (spam)] - " + event.getMessage().toPlain());

			chatOffenceData.trackLastOffence(player.getUniqueId(), message, "spam", points);

			event.setMessageCancelled(true);
		}

		if (!containsPlayer && wordChecker.isCharacterSpam(message)) {
			int points = chatConfig.characterspam.mutepoints;
			mutePointsIncurred += points;
			dataStore.addMutepoints(uuid, points);

			player.sendMessage(Util.withWatermark(TextColors.RED,
                    String.format(
                            DTPunishment.getMessages().PLAYER_SPAMMING_CHARACTERS_MESSAGE,
                    points, dataStore.getMutepoints(uuid))
                    ));

			for (Player p : Sponge.getServer().getOnlinePlayers()) {
				if (p.hasPermission("dtpunishment.staff.notify")) {
					p.sendMessage(Util.withWatermark(TextColors.RED,
							String.format(
									DTPunishment.getMessages().PLAYER_SPAMMING_CHARACTERS_STAFF,
									player.getName(), points, dataStore.getMutepoints(uuid))));
				}
			}
			
			logger.info("[Message cancelled (character spam)] - " + event.getMessage().toPlain());

			chatOffenceData.trackLastOffence(player.getUniqueId(), message, "character spam", points);

			event.setMessageCancelled(true);
		}

		if (wordChecker.containsBannedWords(message)) {
			int points = chatConfig.banned.mutepoints;
			mutePointsIncurred += points;
			dataStore.addMutepoints(uuid, points);
			String bannedWord = wordChecker.getBannedWord(message);

			player.sendMessage(Util.withWatermark(TextColors.RED,
					String.format(
							DTPunishment.getMessages().PLAYER_SAID_BANNED_WORD,
							bannedWord, points, dataStore.getMutepoints(uuid))));

			for (Player p : Sponge.getServer().getOnlinePlayers()) {
				if (p.hasPermission("dtpunishment.staff.notify")) {
					p.sendMessage(Util.withWatermark(TextColors.RED,
							String.format(
									DTPunishment.getMessages().PLAYER_SAID_BANNED_WORD_STAFF,
									player.getName(), bannedWord, points, dataStore.getMutepoints(uuid))));
				}
			}

			logger.info("[Bad message (banned words)] - " + event.getMessage().toPlain());

			chatOffenceData.trackLastOffence(player.getUniqueId(), message, "banned words", points);

			boolean cancel = false;
			if (DTPunishment.getChatConfig().banned.replacer.containsKey(bannedWord)) {
                event.setMessage(TextSerializers.FORMATTING_CODE.deserialize(TextSerializers.FORMATTING_CODE.serialize(event.getMessage()).replace(bannedWord, DTPunishment.getChatConfig().banned.replacer.get(bannedWord))));
            } else if (DTPunishment.getChatConfig().banned.starBadWords) {
                event.setMessage(TextSerializers.FORMATTING_CODE.deserialize(TextSerializers.FORMATTING_CODE.serialize(event.getMessage()).replace(bannedWord, "****")));
            } else {
			    event.setCancelled(true);
            }

		}

		if (wordChecker.containsUppercase(message)) {
			int points = chatConfig.caps.mutepoints;
			mutePointsIncurred += points;

			dataStore.addMutepoints(uuid, points);

			player.sendMessage(Util.withWatermark(TextColors.RED,
					String.format(
					DTPunishment.getMessages().PLAYER_EXCEEDED_MAX_CAPS,
                            points, dataStore.getMutepoints(uuid)
                    )));

			for (Player p : Sponge.getServer().getOnlinePlayers()) {
				if (p.hasPermission("dtpunishment.staff.notify")) {
					p.sendMessage(Util.withWatermark(TextColors.RED,
							String.format(
                            DTPunishment.getMessages().PLAYER_EXCEEDED_MAX_CAPS_STAFF,
                            player.getName(), points, dataStore.getMutepoints(uuid)
                            )
                    ));
				}
			}

			logger.info("[Message cancelled (uppercase)] - " + event.getMessage().toPlain());

			chatOffenceData.trackLastOffence(player.getUniqueId(), message, "uppercase", points);

			event.setMessageCancelled(true);
		}

		if (mutePointsIncurred > 0) {
			logger.info(String.format("%s just incurred %d mutepoints", player.getName(), mutePointsIncurred));
			MutepointsPunishment.check(uuid, dataStore.getMutepoints(uuid));
		}
	}

}
