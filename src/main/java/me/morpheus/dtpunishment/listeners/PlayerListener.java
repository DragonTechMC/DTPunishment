package me.morpheus.dtpunishment.listeners;


import me.morpheus.dtpunishment.ChatWatcher;
import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.PunishmentManager;
import me.morpheus.dtpunishment.utils.ConfigUtil;
import me.morpheus.dtpunishment.utils.DBUtil;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerListener {

    private DTPunishment main;

    public PlayerListener(DTPunishment main){
        this.main = main;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {

        if (ConfigUtil.DB_ENABLED) {
            if (!DBUtil.userExists(event.getTargetEntity().getName())) {
                DBUtil.createUser(event.getTargetEntity().getName());
            }
        } else {
            Player p = event.getTargetEntity();
            Path playerData = Paths.get(main.getConfigPath() + "/data/" + p.getName() + ".conf");
            if (Files.notExists(playerData)) {
                main.getLogger().info("No data file has been found for " + p.getName());
                main.getLogger().info("Creating player file");
                try {
                    Files.createFile(playerData);
                    ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), p.getName());
                    playerNode.getNode("points", "banpoints").setValue(0);
                    playerNode.getNode("points", "mutepoints").setValue(0);
                    playerNode.getNode("mute", "isMuted").setValue(false);
                    ConfigUtil.save(main.getConfigPath(), p.getName(), playerNode);
                    main.getLogger().info("Success");
                } catch (IOException e) {
                    main.getLogger().info("Error while creating player file");
                    e.printStackTrace();
                }
            }
        }

        int day = LocalDateTime.now().toLocalDate().getDayOfMonth();
        main.getLogger().info(""+day+main.getConfigPath());
        if (day == 11) {
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
                        playerNode.getNode("points", "mutepoints").setValue(actualmp - 5);
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
                    playerNode.getNode("points", "mutepoints").setValue(actualmp - 5);
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
        ConfigurationNode rootNode = null;
        try {
            rootNode = main.getDefaultConfigLoader().load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String message = event.getRawMessage().toPlain();
        ChatWatcher chatw = new ChatWatcher(main);


        if (ConfigUtil.DB_ENABLED) {
            if (DBUtil.isMuted(player.getName())) {
                Instant expiration = Instant.parse(DBUtil.getUntil(player.getName()));
                if (Instant.now().isAfter(expiration)) {
                    DBUtil.unmute(player.getName());
                } else {
                    main.getLogger().info("[Message cancelled] - " + event.getMessage().toPlain());
                    event.setMessageCancelled(true);
                }
            } else {
                if (chatw.containBannedWords(message)) {
                    int points = rootNode.getNode("chat", "banned", "mutepoints").getInt();
                    DBUtil.addMutepoints(player.getName(), points);
                    player.sendMessage(Text.of(TextColors.RED, "You said a banned word; " +
                            points + " mutepoint(s) have been added automatically, you now have " +
                            DBUtil.getMutepoints(player.getName()) +
                            ". If you believe this is an error, contact a staff member."));
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (p.hasPermission("dtpunishment.staff.notify")) {
                            p.sendMessage(Text.of(TextColors.RED, player.getName() + " said a banned word; " +
                                    points + " mutepoint(s) have been added automatically, they now have " +
                                    DBUtil.getMutepoints(player.getName())));
                        }
                    }
                    event.setMessageCancelled(true);
                }

                if (chatw.containUppercase(message)) {
                    int points = rootNode.getNode("chat", "caps", "mutepoints").getInt();
                    DBUtil.addMutepoints(player.getName(), points);
                    player.sendMessage(Text.of(TextColors.RED, "You have exceeded the max percentage of caps allowed; " +
                            points + " mutepoint(s) have been added automatically, you now have " +
                            DBUtil.getMutepoints(player.getName()) +
                            ". If you believe this is an error, contact a staff member."));
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (p.hasPermission("dtpunishment.staff.notify")) {
                            p.sendMessage(Text.of(player.getName() + " has exceeded the max percentage of caps allowed; " +
                                    points + " mutepoint(s) have been added automatically, they now have " +
                                    DBUtil.getMutepoints(player.getName())));
                        }
                    }
                    event.setMessageCancelled(true);
                }

                PunishmentManager pm = new PunishmentManager(main);
                pm.checkPenalty(player.getName(), "mutepoints", DBUtil.getMutepoints(player.getName()));
            }
        } else {
            ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), player.getName());
            boolean isMuted = playerNode.getNode("mute", "isMuted").getBoolean();

            if (isMuted) {
                Instant expiration = Instant.parse(playerNode.getNode("mute", "until").getString());
                if (Instant.now().isAfter(expiration)) {
                    playerNode.getNode("mute", "isMuted").setValue(false);
                    playerNode.getNode("mute").removeChild("until");
                    ConfigUtil.save(main.getConfigPath(), player.getName(), playerNode);
                } else {
                    main.getLogger().info("[Message cancelled] - " + event.getMessage().toPlain());
                    event.setMessageCancelled(true);
                }
            } else {

                if (chatw.containBannedWords(message)) {
                    int points = rootNode.getNode("chat", "banned", "mutepoints").getInt();
                    int actual = playerNode.getNode("points", "mutepoints").getInt();
                    playerNode.getNode("points", "mutepoints").setValue(actual + points);
                    ConfigUtil.save(main.getConfigPath(), player.getName(), playerNode);
                    player.sendMessage(Text.of(TextColors.RED, "You said a banned word; " +
                            points + " mutepoint(s) have been added automatically, you now have " +
                            playerNode.getNode("points", "mutepoints").getInt() +
                            ". If you believe this is an error, contact a staff member."));
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (p.hasPermission("dtpunishment.staff.notify")) {
                            p.sendMessage(Text.of(TextColors.RED, player.getName() + " said a banned word; " +
                                    points + " mutepoint(s) have been added automatically, they now have " +
                                    playerNode.getNode("points", "mutepoints").getInt()));
                        }
                    }
                    event.setMessageCancelled(true);
                }

                if (chatw.containUppercase(message)) {
                    int points = rootNode.getNode("chat", "caps", "mutepoints").getInt();
                    int actual = playerNode.getNode("points", "mutepoints").getInt();
                    playerNode.getNode("points", "mutepoints").setValue(actual + points);
                    ConfigUtil.save(main.getConfigPath(), player.getName(), playerNode);
                    player.sendMessage(Text.of(TextColors.RED, "You have exceeded the max percentage of caps allowed; " +
                            points + " mutepoint(s) have been added automatically, you now have " +
                            playerNode.getNode("points", "mutepoints").getInt() +
                            ". If you believe this is an error, contact a staff member."));
                    for (Player p : Sponge.getServer().getOnlinePlayers()) {
                        if (p.hasPermission("dtpunishment.staff.notify")) {
                            p.sendMessage(Text.of(TextColors.RED, player.getName() + " has exceeded the max percentage of caps allowed;  " +
                                    points + " mutepoint(s) have been added automatically, they now have " +
                                    playerNode.getNode("points", "mutepoints").getInt()));
                        }
                    }
                    event.setMessageCancelled(true);
                }


                PunishmentManager pm = new PunishmentManager(main);
                pm.checkPenalty(player.getName(), "mutepoints", playerNode.getNode("points", "mutepoints").getInt());
            }
        }
    }


}
