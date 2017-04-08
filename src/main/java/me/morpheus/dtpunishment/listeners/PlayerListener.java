package me.morpheus.dtpunishment.listeners;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.morpheus.dtpunishment.ChatWatcher;
import me.morpheus.dtpunishment.configuration.ChatConfig;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.penalty.MutepointsPunishment;
import me.morpheus.dtpunishment.utils.Util;

public class PlayerListener {

    @Inject
    private Logger logger;

    @Inject
    private DataStore dataStore;

    @Inject
    private ChatWatcher chatWatcher;

    @Inject
    private ChatConfig chatConfig;

    @Inject
    private MutepointsPunishment mutePunish;

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {

        UUID uuid = event.getTargetEntity().getUniqueId();
        if (!dataStore.userExists(uuid)) {
            logger.info(event.getTargetEntity().getName() + " not found, creating player data...");
            dataStore.createUser(uuid);
        } else {
            LocalDate now = LocalDate.now();
            if (now.isAfter(dataStore.getMutepointsUpdatedAt(uuid).plusMonths(1))) {
                int actual = dataStore.getMutepoints(uuid);
                int amount = (actual - 5 < 0) ? actual : 5;
                dataStore.removeMutepoints(uuid, amount);
                dataStore.addMutepoints(uuid, 0);
            }
            if (now.isAfter(dataStore.getBanpointsUpdatedAt(uuid).plusMonths(1))) {
                int actual = dataStore.getBanpoints(uuid);
                int amount = (actual - 1 < 0) ? actual : 1;
                dataStore.removeBanpoints(uuid, amount);
                dataStore.addBanpoints(uuid, 0);
            }
        }

        dataStore.finish();
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
                event.setMessageCancelled(true);
            }
        } else {

            if (chatWatcher.isSpam(message, uuid)) {
                int points = chatConfig.spam.mutepoints;
                mutePointsIncurred += points;
                dataStore.addMutepoints(uuid, points);
                event.setMessageCancelled(true);
            }

            if (chatWatcher.containBannedWords(message)) {
                int points = chatConfig.banned.mutepoints;
                mutePointsIncurred += points;
                dataStore.addMutepoints(uuid, points);

                player.sendMessage(Util.getWatermark().append(Text.of(TextColors.RED, "You said a banned word; "
                        + points + " mutepoint(s) have been added automatically, you now have "
                        + dataStore.getMutepoints(uuid) + ". If you believe this is an error, contact a staff member."))
                        .build());

                for (Player p : Sponge.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("dtpunishment.staff.notify")) {
                        p.sendMessage(Util.getWatermark()
                                .append(Text.of(TextColors.RED,
                                        player.getName() + " said a banned word; " + points
                                                + " mutepoint(s) have been added automatically, they now have "
                                                + dataStore.getMutepoints(uuid)))
                                .build());
                    }
                }

                event.setMessageCancelled(true);

            }

            if (chatWatcher.containUppercase(message)) {
                int points = chatConfig.caps.mutepoints;
                mutePointsIncurred += points;

                dataStore.addMutepoints(uuid, points);

                player.sendMessage(Util.getWatermark()
                        .append(Text.of(TextColors.RED,
                                "You have exceeded the max percentage of caps allowed; " + points
                                        + " mutepoint(s) have been added automatically, you now have "
                                        + dataStore.getMutepoints(uuid)
                                        + ". If you believe this is an error, contact a staff member."))
                        .build());
                for (Player p : Sponge.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("dtpunishment.staff.notify")) {
                        p.sendMessage(Util.getWatermark()
                                .append(Text.of(TextColors.RED,
                                        player.getName() + " has exceeded the max percentage of caps allowed;  "
                                                + points + " mutepoint(s) have been added automatically, they now have "
                                                + dataStore.getMutepoints(uuid)))
                                .build());
                    }
                }
                event.setMessageCancelled(true);
            }

            logger.info(String.format("%s just incurred %d mutepoints", player.getName(), mutePointsIncurred));

            if (mutePointsIncurred > 0) {
                mutePunish.check(uuid, dataStore.getMutepoints(uuid));
            }

            dataStore.finish();
        }
    }

}
