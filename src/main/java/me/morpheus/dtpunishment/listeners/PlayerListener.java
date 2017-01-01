package me.morpheus.dtpunishment.listeners;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.utils.ConfigUtil;
import me.morpheus.dtpunishment.utils.DBUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

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
    }

    @Listener
    public void onPlayerChat(MessageChannelEvent.Chat event, @Root Player player){
        if (ConfigUtil.DB_ENABLED) {
            if (DBUtil.isMuted(player.getName())) {
                Instant expiration = Instant.parse(DBUtil.getUntil(player.getName()));
                if (Instant.now().isAfter(expiration)) {
                    DBUtil.unmute(player.getName());
                } else {
                    main.getLogger().info("[Message cancelled] - " + event.getMessage().toPlain());
                    event.setCancelled(true);
                }
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
                    event.setCancelled(true);
                }
            }
        }
    }


}
