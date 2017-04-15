package me.morpheus.dtpunishment;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;

import me.morpheus.dtpunishment.commands.CommandManager;
import me.morpheus.dtpunishment.configuration.ConfigurationManager;
import me.morpheus.dtpunishment.configuration.MainConfig;
import me.morpheus.dtpunishment.configuration.PunishmentLength;
import me.morpheus.dtpunishment.configuration.PunishmentLengthSerializer;
import me.morpheus.dtpunishment.listeners.PlayerListener;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

@Plugin(id = "dtpunishment", name = "DTPunishment", description = "Dragon Tech Punishment: a unique chat safeguard plugin")
public class DTPunishment {

	private Logger logger;

	private Injector injector;

	private Injector childInjector;

	private MainConfig config;

	private CommandManager commandManager;

	@Inject
	public DTPunishment(Logger logger, Injector injector, MainConfig mainConfig) {
		this.logger = logger;
		this.injector = injector;
		this.config = mainConfig;
		registerSerializers();
	}

	private void registerSerializers() {
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(PunishmentLength.class),
				new PunishmentLengthSerializer());
	}

	@Listener
	public void onServerPreInit(GamePreInitializationEvent event) {
		logger.info("Enabling DTPunishment...");

		// Create the child injector for the plugin
		childInjector = injector.createChildInjector(new DTPunishmentModule(config));

		// Get config manager and init
		ConfigurationManager configurationManager = childInjector.getInstance(ConfigurationManager.class);
		configurationManager.intialise();

		// Get command manager from child injector
		commandManager = childInjector.getInstance(CommandManager.class);
	}

	@Listener
	public void onServerInit(GameInitializationEvent event) {
		logger.info("Registering listeners and commands...");
		Sponge.getEventManager().registerListeners(this, childInjector.getInstance(PlayerListener.class));

		// Register any commands
		commandManager.registerCommands();
	}

}
