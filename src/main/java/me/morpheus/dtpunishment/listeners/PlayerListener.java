package me.morpheus.dtpunishment.listeners;


import me.morpheus.dtpunishment.ChatWatcher;
import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.penalty.MutepointsPunishment;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class PlayerListener {

    private DTPunishment main;

    public PlayerListener(DTPunishment main){
        this.main = main;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {

        UUID uuid = event.getTargetEntity().getUniqueId();
        if (!main.getDatastore().userExists(uuid)) {
            main.getLogger().info(event.getTargetEntity().getName() + " not found, creating player data...");
            main.getDatastore().createUser(uuid);
        } else {
            LocalDate now = LocalDate.now();
            if (main.getDatastore().getMutepointsUpdatedAt(uuid) != null && now.isAfter(main.getDatastore().getMutepointsUpdatedAt(uuid).plusMonths(1))) {
                int actual = main.getDatastore().getMutepoints(uuid);
                int amount = (actual - 5 < 0) ? actual : 5;
                main.getDatastore().removeMutepoints(uuid, amount);
                main.getDatastore().addMutepoints(uuid, 0);
            }
            if (main.getDatastore().getBanpointsUpdatedAt(uuid) != null && now.isAfter(main.getDatastore().getBanpointsUpdatedAt(uuid).plusMonths(1))) {
                int actual = main.getDatastore().getBanpoints(uuid);
                int amount = (actual - 1 < 0) ? actual : 1;
                main.getDatastore().removeBanpoints(uuid, amount);
                main.getDatastore().addBanpoints(uuid, 0);
            }
        }

        main.getDatastore().finish();


    }


    @Listener
    public void onPlayerChat(MessageChannelEvent.Chat event, @Root Player player){

        String message = event.getRawMessage().toPlain();
        UUID uuid = player.getUniqueId();

        ChatWatcher chatw = new ChatWatcher(main);


        if (main.getDatastore().isMuted(uuid)) {
            Instant expiration = main.getDatastore().getExpiration(uuid);

            if (Instant.now().isAfter(expiration)) {
                main.getDatastore().unmute(uuid);
            } else {
                main.getLogger().info("[Message cancelled] - " + event.getMessage().toPlain());
                event.setMessageCancelled(true);
            }
        } else {

            if (chatw.isSpam(message, uuid)) {
                int points = main.getChatConfig().spam.mutepoints;
                main.getDatastore().addMutepoints(uuid, points);
                event.setMessageCancelled(true);
            }

            if (chatw.containBannedWords(message)) {
                int points = main.getChatConfig().banned.mutepoints;
                main.getDatastore().addMutepoints(uuid, points);

                player.sendMessage(Text.of(TextColors.RED, "You said a banned word; " +
                        points + " mutepoint(s) have been added automatically, you now have " +
                        main.getDatastore().getMutepoints(uuid) +
                        ". If you believe this is an error, contact a staff member."));

                for (Player p : Sponge.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("dtpunishment.staff.notify")) {
                        p.sendMessage(Text.of(TextColors.RED, player.getName() + " said a banned word; " +
                                points + " mutepoint(s) have been added automatically, they now have " +
                                main.getDatastore().getMutepoints(uuid)));
                    }
                }

                event.setMessageCancelled(true);

            }

            if (chatw.containUppercase(message)) {
                int points = main.getChatConfig().caps.mutepoints;

                main.getDatastore().addMutepoints(uuid, points);

                player.sendMessage(Text.of(TextColors.RED, "You have exceeded the max percentage of caps allowed; " +
                        points + " mutepoint(s) have been added automatically, you now have " +
                        main.getDatastore().getMutepoints(uuid) +
                        ". If you believe this is an error, contact a staff member."));
                for (Player p : Sponge.getServer().getOnlinePlayers()) {
                    if (p.hasPermission("dtpunishment.staff.notify")) {
                        p.sendMessage(Text.of(TextColors.RED, player.getName() + " has exceeded the max percentage of caps allowed;  " +
                                points + " mutepoint(s) have been added automatically, they now have " +
                                main.getDatastore().getMutepoints(uuid)));
                    }
                }
                event.setMessageCancelled(true);
            }



            MutepointsPunishment mutepunish = new MutepointsPunishment(main);

            mutepunish.check(uuid, main.getDatastore().getMutepoints(uuid));

            main.getDatastore().finish();

        }
    }


}
