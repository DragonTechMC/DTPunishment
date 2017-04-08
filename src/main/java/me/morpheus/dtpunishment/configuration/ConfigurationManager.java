package me.morpheus.dtpunishment.configuration;

import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Singleton
public class ConfigurationManager {

    @Inject
    private Logger logger;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    @Inject
    private ChatConfig chatConfig;

    @Inject
    private MainConfig mainConfig;

    HoconConfigurationLoader mainConfigLoader;
    ObjectMapper<MainConfig> mainConfigMapper;

    HoconConfigurationLoader chatConfigLoader;
    ObjectMapper<ChatConfig> chatConfigMapper;

    public void intialise() {
        createLoaders();
        generateConfig();
        loadConfiguration();
    }

    private void createLoaders() {
        try {
            mainConfigMapper = ObjectMapper.forClass(MainConfig.class);
            chatConfigMapper = ObjectMapper.forClass(ChatConfig.class);
            mainConfigLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
            chatConfigLoader = HoconConfigurationLoader.builder().setPath(privateConfigDir.resolve("chat.conf"))
                    .build();
        } catch (ObjectMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadConfiguration() {
        try {
            mainConfigMapper.bind(mainConfig).populate(mainConfigLoader.load());
            chatConfigMapper.bind(chatConfig).populate(chatConfigLoader.load());

            logger.info("Loaded configuration files");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateConfig() {
        if (Files.notExists(defaultConfig)) {
            logger.warn("Config not found. Generating default config...");

            try {
                CommentedConfigurationNode root = mainConfigLoader.load();
                mainConfigMapper.bind(mainConfig).serialize(root);
                logger.info("config: " + root);
                mainConfigLoader.save(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Files.notExists(privateConfigDir.resolve("chat.conf"))) {
            logger.warn("Chat config not found. Generating chat config...");

            try {
                CommentedConfigurationNode root = chatConfigLoader.load();
                chatConfigMapper.bindToNew().serialize(root);
                logger.info("config: " + root);
                chatConfigLoader.save(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
