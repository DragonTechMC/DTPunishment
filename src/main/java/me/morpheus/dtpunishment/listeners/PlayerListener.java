package me.morpheus.dtpunishment.listeners;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Server;
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

public class PlayerListener {

	private Logger logger;

	private DataStore dataStore;

	private WordChecker wordChecker;

	private ChatConfig chatConfig;

	private MutepointsPunishment mutePunish;

	private Server server;

	private ChatOffenceData chatOffenceData;

	@Inject
	public PlayerListener(Logger logger, DataStore dataStore, WordChecker wordChecker, ChatConfig chatConfig,
			MutepointsPunishment mutePunish, Server server, ChatOffenceData chatOffenceData) {
		this.logger = logger;
		this.dataStore = dataStore;
		this.wordChecker = wordChecker;
		this.chatConfig = chatConfig;
		this.mutePunish = mutePunish;
		this.server = server;
		this.chatOffenceData = chatOffenceData;
	}

	@Listener
	public void onPlayerPreJoin(ClientConnectionEvent.Login event) {
		User user = event.getTargetUser();

		if (wordChecker.containsBannedWords(user.getName())) {

			String word = wordChecker.getBannedWord(user.getName());

			event.setMessage(Util.withWatermark(TextColors.AQUA, TextStyles.BOLD,
					String.format(
							"You cannot join the server because your username contains a banned word - the word is '%s'",
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
								"You have been muted until %s due to violating chat policy. If you believe this is an error, contact a staff member",
								Util.instantToString(expiration), dataStore.getMutepoints(uuid))));

				event.setMessageCancelled(true);
			}
		} else {

			if (wordChecker.isSpam(message, uuid)) {
				int points = chatConfig.spam.mutepoints;
				mutePointsIncurred += points;
				dataStore.addMutepoints(uuid, points);

				player.sendMessage(Util.withWatermark(TextColors.RED, "You are spamming chat, " + points
						+ " mutepoint(s) have been added automatically, you now have " + dataStore.getMutepoints(uuid)
						+ ". If you believe this is an error, contact a staff member."));

				logger.info("[Message cancelled (spam)] - " + event.getMessage().toPlain());

				chatOffenceData.trackLastOffence(player.getUniqueId(), message, "spam", points);

				event.setMessageCancelled(true);
			}

			if (wordChecker.containsBannedWords(message)) {
				int points = chatConfig.banned.mutepoints;
				mutePointsIncurred += points;
				dataStore.addMutepoints(uuid, points);
				String bannedWord = wordChecker.getBannedWord(message);

				player.sendMessage(Util.withWatermark(TextColors.RED,
						String.format(
								"You said a banned word '%s'; %d mutepoint(s) have been added automatically, you now have "
										+ "%d. If you believe this is an error, contact a staff member.",
								bannedWord, points, dataStore.getMutepoints(uuid))));

				for (Player p : server.getOnlinePlayers()) {
					if (p.hasPermission("dtpunishment.staff.notify")) {
						p.sendMessage(Util.withWatermark(TextColors.RED,
								String.format(
										"%s said a banned word '%s'; %d"
												+ " mutepoint(s) have been added automatically, they now have %d",
										player.getName(), bannedWord, points, dataStore.getMutepoints(uuid))));
					}
				}

				logger.info("[Message cancelled (banned words)] - " + event.getMessage().toPlain());

				chatOffenceData.trackLastOffence(player.getUniqueId(), message, "banned words", points);

				event.setMessageCancelled(true);

			}

			if (wordChecker.containsUppercase(message)) {
				int points = chatConfig.caps.mutepoints;
				mutePointsIncurred += points;

				dataStore.addMutepoints(uuid, points);

				player.sendMessage(Util.withWatermark(TextColors.RED,
						"You have exceeded the max percentage of caps allowed; " + points
								+ " mutepoint(s) have been added automatically, you now have "
								+ dataStore.getMutepoints(uuid)
								+ ". If you believe this is an error, contact a staff member."));

				for (Player p : server.getOnlinePlayers()) {
					if (p.hasPermission("dtpunishment.staff.notify")) {
						p.sendMessage(Util.withWatermark(TextColors.RED,
								player.getName() + " has exceeded the max percentage of caps allowed;  " + points
										+ " mutepoint(s) have been added automatically, they now have "
										+ dataStore.getMutepoints(uuid)));
					}
				}

				logger.info("[Message cancelled (uppercase)] - " + event.getMessage().toPlain());

				chatOffenceData.trackLastOffence(player.getUniqueId(), message, "uppercase", points);

				event.setMessageCancelled(true);
			}

			if (mutePointsIncurred > 0) {
				logger.info(String.format("%s just incurred %d mutepoints", player.getName(), mutePointsIncurred));
				mutePunish.check(uuid, dataStore.getMutepoints(uuid));
			}
		}
	}

}