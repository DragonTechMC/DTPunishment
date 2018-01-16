package me.morpheus.dtpunishment;

import me.morpheus.dtpunishment.configuration.*;
import me.morpheus.dtpunishment.data.ChatOffenceData;
import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.data.FileDataStore;
import me.morpheus.dtpunishment.data.MySqlDataStore;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;

import me.morpheus.dtpunishment.commands.CommandManager;
import me.morpheus.dtpunishment.listeners.PlayerListener;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

import java.io.File;
import java.nio.file.Path;

@Plugin(id = "dtpunishment", name = "DTPunishment", description = "Dragon Tech Punishment: a unique chat safeguard plugin")
public class DTPunishment {

    @Inject
	public Logger logger;

    public static Logger getLogger() {
        return instance.logger;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
	public File configDir;

    public File dataDir;

    @Inject
    public GuiceObjectMapperFactory mapper;

    public DataStore dataStore;

	private static DTPunishment instance;

	private ConfigManager<ChatConfig> chatConfig;
    private ConfigManager<MainConfig> mainConfig;
    private ConfigManager<Messages> messages;

	@Listener
	public void onServerPreInit(GamePreInitializationEvent event) {
	    instance = this;

        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(PunishmentLength.class),
                new PunishmentLengthSerializer());

	    this.chatConfig = new ConfigManager<>(ChatConfig.class, "chat.conf", configDir, mapper);
	    this.mainConfig = new ConfigManager<>(MainConfig.class, "DTPunishments.conf", configDir, mapper);
	    this.messages = new ConfigManager<>(Messages.class, "Messages.conf", configDir, mapper);

	    File file;
	    if (getConfig().useCustomDataDirectory && !getConfig().customDataDirectory.isEmpty()) {
	        file = new File(new File(getConfig().customDataDirectory), "data");
        } else {
	        file = new File(this.configDir, "data");
        }
	    if (!file.exists()) file.mkdirs();
	    this.dataDir = file;

	    if (this.mainConfig.getConfig().database.enabled) {
	        this.dataStore = new MySqlDataStore();
        } else {
	        this.dataStore = new FileDataStore();
        }

		logger.info("Enabling DTPunishment...");

		// Get command manager from child injector
		commandManager = new CommandManager();
		wordChecker = new WordChecker();
		this.offenceData = new ChatOffenceData();
	}

	@Listener
	public void onServerInit(GameInitializationEvent event) {
		logger.info("Registering listeners and commands...");
		Sponge.getEventManager().registerListeners(this, new PlayerListener());

		// Register any commands
		commandManager.registerCommands();
	}

	@Listener
    public void onReload(GameReloadEvent e) {
	    this.mainConfig.load();
	    this.chatConfig.load();
	    this.messages.load();
    }

	public static DTPunishment getInstance() {
	    return instance;
    }

    public static MainConfig getConfig() {
	    return instance.mainConfig.getConfig();
    }

    public static Messages getMessages() {
	    return instance.messages.getConfig();
    }

    public static ChatConfig getChatConfig() {
	    return instance.chatConfig.getConfig();
    }

    public static DataStore getDataStore() {
	    return instance.dataStore;
    }

    private CommandManager commandManager;
	public static CommandManager getCommandManager() {
	    return instance.commandManager;
    }

    private WordChecker wordChecker;
	public static WordChecker getWordChecker() {
	    return instance.wordChecker;
    }

    private ChatOffenceData offenceData;
    public static ChatOffenceData getOffenceData() {
        return instance.offenceData;
    }

    public static File getDataDirectory() {
        return instance.dataDir;
    }

}
