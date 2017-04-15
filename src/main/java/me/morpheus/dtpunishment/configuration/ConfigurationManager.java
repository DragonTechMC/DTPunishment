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

	private Logger logger;

	private Path defaultConfig;

	private Path privateConfigDir;

	private ChatConfig chatConfig;

	private MainConfig mainConfig;

	private HoconConfigurationLoader mainConfigLoader;
	private ObjectMapper<MainConfig>.BoundInstance boundMainConfig;

	private HoconConfigurationLoader chatConfigLoader;
	private ObjectMapper<ChatConfig>.BoundInstance boundChatConfig;

	@Inject
	public ConfigurationManager(Logger logger, @DefaultConfig(sharedRoot = false) Path defaultConfig,
			@ConfigDir(sharedRoot = false) Path privateConfigDir, ChatConfig chatConfig, MainConfig mainConfig)
			throws Exception {
		this.logger = logger;
		this.defaultConfig = defaultConfig;
		this.privateConfigDir = privateConfigDir;
		this.chatConfig = chatConfig;
		this.mainConfig = mainConfig;
	}

	public void intialise() {
		createLoaders();
		loadConfiguration();
	}

	private void createLoaders() {
		try {
			ObjectMapper<MainConfig> mainConfigMapper = ObjectMapper.forClass(MainConfig.class);
			ObjectMapper<ChatConfig> chatConfigMapper = ObjectMapper.forClass(ChatConfig.class);
			mainConfigLoader = HoconConfigurationLoader.builder().setPath(defaultConfig).build();
			chatConfigLoader = HoconConfigurationLoader.builder().setPath(privateConfigDir.resolve("chat.conf"))
					.build();

			boundMainConfig = mainConfigMapper.bind(mainConfig);
			boundChatConfig = chatConfigMapper.bind(chatConfig);

		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
	}

	public void loadConfiguration() {
		try {
			generateConfig();

			boundMainConfig.populate(mainConfigLoader.load());
			boundChatConfig.populate(chatConfigLoader.load());

			logger.info("Loaded configuration files");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void generateConfig() {
		if (Files.notExists(defaultConfig)) {
			logger.warn("Config not found. Generating default config...");

			try {
				CommentedConfigurationNode root = mainConfigLoader.createEmptyNode();
				boundMainConfig.serialize(root);
				mainConfigLoader.save(root);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (Files.notExists(privateConfigDir.resolve("chat.conf"))) {
			logger.warn("Chat config not found. Generating chat config...");

			try {
				CommentedConfigurationNode root = chatConfigLoader.createEmptyNode();
				boundChatConfig.serialize(root);
				chatConfigLoader.save(root);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
