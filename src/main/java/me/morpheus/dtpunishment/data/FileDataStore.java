package me.morpheus.dtpunishment.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;

import com.google.inject.Inject;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class FileDataStore implements DataStore {

    @Inject
    private Logger logger;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    private ConfigurationLoader<CommentedConfigurationNode> loader;

    private ConfigurationNode node;

    private void save() {
        try {
            loader.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initPlayerConfig(UUID player) {
        Path playerData = configDir.resolve(String.format("data/%s.conf", player));
        loader = HoconConfigurationLoader.builder().setPath(playerData).build();
        try {
            node = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        Path dataFolder = configDir.resolve("data");
        if (Files.exists(dataFolder)) {
            logger.info("Data folder found");
        } else {
            try {
                Files.createDirectories(dataFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getBanpoints(UUID player) {
        initPlayerConfig(player);
        return node.getNode("points", "banpoints").getInt();
    }

    @Override
    public LocalDate getBanpointsUpdatedAt(UUID player) {
        initPlayerConfig(player);
        String bUpdatedAt = node.getNode("points", "bUpdatedAt").getString();
        return LocalDate.parse(bUpdatedAt);
    }

    @Override
    public int getMutepoints(UUID player) {
        initPlayerConfig(player);
        return node.getNode("points", "mutepoints").getInt();
    }

    @Override
    public LocalDate getMutepointsUpdatedAt(UUID player) {
        initPlayerConfig(player);
        String mUpdatedAt = node.getNode("points", "mUpdatedAt").getString();
        return LocalDate.parse(mUpdatedAt);
    }

    @Override
    public boolean isMuted(UUID player) {
        initPlayerConfig(player);
        return node.getNode("mute", "isMuted").getBoolean();
    }

    @Override
    public Instant getExpiration(UUID player) {
        initPlayerConfig(player);
        String exp = node.getNode("mute", "until").getString();
        return Instant.parse(exp);
    }

    @Override
    public void addBanpoints(UUID player, int amount) {
        initPlayerConfig(player);
        int actual = getBanpoints(player);
        node.getNode("points", "banpoints").setValue(actual + amount);
        String now = String.valueOf(LocalDate.now());
        node.getNode("points", "bUpdatedAt").setValue(now);
        save();
    }

    @Override
    public void removeBanpoints(UUID player, int amount) {
        initPlayerConfig(player);
        int actual = getBanpoints(player);
        node.getNode("points", "banpoints").setValue(actual - amount);
        save();
    }

    @Override
    public void addMutepoints(UUID player, int amount) {
        initPlayerConfig(player);
        int actual = getMutepoints(player);
        node.getNode("points", "mutepoints").setValue(actual + amount);
        String now = String.valueOf(LocalDate.now());
        node.getNode("points", "mUpdatedAt").setValue(now);
        save();
    }

    @Override
    public void removeMutepoints(UUID player, int amount) {
        initPlayerConfig(player);
        int actual = getMutepoints(player);
        node.getNode("points", "mutepoints").setValue(actual - amount);
        save();
    }

    @Override
    public void mute(UUID player, Instant expiration) {
        initPlayerConfig(player);
        node.getNode("mute", "isMuted").setValue(true);
        String exp = String.valueOf(expiration);
        node.getNode("mute", "until").setValue(exp);
        save();
    }

    @Override
    public void unmute(UUID player) {
        initPlayerConfig(player);
        node.getNode("mute", "isMuted").setValue(false);
        node.getNode("mute").removeChild("until");
        save();
    }

    @Override
    public void createUser(UUID player) {
        Path playerData = configDir.resolve(String.format("data/%s.conf", player));

        try {
            Files.createFile(playerData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initPlayerConfig(player);
        node.getNode("points", "bUpdatedAt").setValue(String.valueOf(LocalDate.now()));
        node.getNode("points", "banpoints").setValue(0);
        node.getNode("points", "mUpdatedAt").setValue(String.valueOf(LocalDate.now()));
        node.getNode("points", "mutepoints").setValue(0);
        node.getNode("mute", "isMuted").setValue(false);
        save();
    }

    @Override
    public boolean userExists(UUID player) {
        Path playerData = configDir.resolve(String.format("data/%s.conf", player));
        return Files.exists(playerData);
    }

    @Override
    public void finish() {

    }
}
