package me.morpheus.dtpunishment.listeners;


import me.morpheus.dtpunishment.ChatWatcher;
import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.penalty.MutepointsPunishment;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class PlayerListener {

    private DTPunishment main;

    public PlayerListener(DTPunishment main){
        this.main = main;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {

        if (!main.getDatastore().userExists(event.getTargetEntity().getUniqueId())) {
            main.getLogger().info(event.getTargetEntity().getName() + " not found, creating player data...");
            main.getDatastore().createUser(event.getTargetEntity().getUniqueId());
        }

        int day = LocalDateTime.now().toLocalDate().getDayOfMonth();
        if (day == 1) {
            File data = new File(main.getConfigPath() + "/data/");
            for (File f : data.listFiles()) {
                ConfigurationLoader<CommentedConfigurationNode> loader =
                        HoconConfigurationLoader.builder().setPath(f.toPath()).build();
                ConfigurationNode playerNode = null;
                try {
                    playerNode = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                boolean received = playerNode.getNode("points", "bonus_received").getBoolean();
                if (!received) {
                    int actualmp = playerNode.getNode("points", "mutepoints").getInt();
                    int actualbp = playerNode.getNode("points", "banpoints").getInt();
                    if (actualbp != 0) {
                        playerNode.getNode("points", "mutepoints").setValue(actualbp - 1);
                    }
                    if (actualmp != 0) {
                        int total = actualmp - 5;
                        if (total<0) total=0;
                        playerNode.getNode("points", "mutepoints").setValue(total);
                    }
                    playerNode.getNode("points", "bonus_received").setValue(true);

                    try {
                        loader.save(playerNode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (day == 2) {
            File data = new File(main.getConfigPath() + "/data/");
            for (File f : data.listFiles()) {
                ConfigurationLoader<CommentedConfigurationNode> loader =
                        HoconConfigurationLoader.builder().setPath(f.toPath()).build();
                ConfigurationNode playerNode = null;
                try {
                    playerNode = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                boolean received = playerNode.getNode("points", "bonus_received").getBoolean();
                if (received) {
                    playerNode.getNode("points", "bonus_received").setValue(false);
                    try {
                        loader.save(playerNode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                int actualmp = playerNode.getNode("points", "mutepoints").getInt();
                int actualbp = playerNode.getNode("points", "banpoints").getInt();
                if (actualbp != 0) {
                    playerNode.getNode("points", "mutepoints").setValue(actualbp - 1);
                }
                if (actualmp != 0) {
                    int total = actualmp - 5;
                    if (total<0) total=0;
                    playerNode.getNode("points", "mutepoints").setValue(total);
                }
                try {
                    loader.save(playerNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
            Instant in = Instant.now();

            if (chatw.isSpam(message, player, in)) {
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
