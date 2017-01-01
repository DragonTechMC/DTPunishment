package me.morpheus.dtpunishment.utils;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigUtil {

    public static boolean DB_ENABLED;

    public static ConfigurationNode getPlayerNode(Path configPath, String player) {
        Path playerData = Paths.get(configPath + "/data/" + player + ".conf");
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(playerData).build();
        ConfigurationNode playerNode = null;
        try {
            playerNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playerNode;
    }

    public static void save(Path configPath, String player, ConfigurationNode node) {
        Path playerData = Paths.get(configPath + "/data/" + player + ".conf");
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(playerData).build();
        try {
            loader.save(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
