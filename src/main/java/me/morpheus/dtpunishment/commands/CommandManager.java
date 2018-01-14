package me.morpheus.dtpunishment.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsAdd;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsRemove;
import me.morpheus.dtpunishment.commands.banpoints.CommandBanpointsShow;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsAdd;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsRemove;
import me.morpheus.dtpunishment.commands.mutepoints.CommandMutepointsShow;
import me.morpheus.dtpunishment.commands.mutepoints.CommandUnmute;
import me.morpheus.dtpunishment.configuration.MainConfig;

public class CommandManager {

	private DTPunishment plugin;

	private List<CommandMapping> mappedCommands;

	private org.spongepowered.api.command.CommandManager spongeCommandManager;

	private Logger logger;

	private MainConfig mainConfig;

	public CommandManager() {
		this.mappedCommands = new ArrayList<CommandMapping>();
		this.logger = DTPunishment.getInstance().logger;
		this.plugin = DTPunishment.getInstance();
		this.mainConfig = DTPunishment.getConfig();
		this.spongeCommandManager = Sponge.getCommandManager();
	}

	public void registerCommands() {
		unregisterCommands();

		registerBanpointsCommands();
		registerMutepointsCommands();
		registerAdminCommands();
	}

	private void tryRegisterCommand(CommandSpec spec, List<String> aliases) {
		Optional<CommandMapping> mapped = spongeCommandManager.register(plugin, spec, aliases);

		if (!mapped.isPresent()) {
			logger.warn("Could not register command under alias(es): " + String.join(", ", aliases)
					+ " - some commands will not be available. You can re-map aliases in the config file.");
		} else {
			mappedCommands.add(mapped.get());
		}
	}

	private void registerBanpointsCommands() {
		CommandSpec showBanpoints = CommandSpec.builder().permission("dtpunishment.banpoints.show")
				.description(Text.of("Show how many banpoints the specified player has"))
				.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))))
				.executor(new CommandBanpointsShow()).build();

		CommandSpec addBanpoints = CommandSpec.builder().permission("dtpunishment.banpoints.add")
				.description(Text.of("Add a specified amount of banpoints to a player"))
				.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))),
						GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
				.executor(new CommandBanpointsAdd()).build();

		CommandSpec removeBanpoints = CommandSpec.builder().permission("dtpunishment.banpoints.remove")
				.description(Text.of("Remove a specified amount of banpoints from a player"))
				.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))),
						GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
				.executor(new CommandBanpointsRemove()).build();

		CommandSpec banpoints = CommandSpec.builder().permission("dtpunishment.banpoints")
				.description(Text.of("Show the Banpoints help menu")).arguments(GenericArguments.none())
				.child(showBanpoints, "show").child(addBanpoints, "add").child(removeBanpoints, "remove").build();

		tryRegisterCommand(banpoints, mainConfig.aliases.banpoints);
	}

	private void registerMutepointsCommands() {

		CommandSpec showMutepoints = CommandSpec.builder().permission("dtpunishment.mutepoints.show")
				.description(Text.of("Show how many Mutepoints the specified player has "))
				.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))))
				.executor(new CommandMutepointsShow()).build();

		CommandSpec addMutepoints = CommandSpec.builder().permission("dtpunishment.mutepoints.add")
				.description(Text.of("Add a specified amount of Mutepoints to a player "))
				.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))),
						GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
				.executor(new CommandMutepointsAdd()).build();

		CommandSpec removeMutepoints = CommandSpec.builder().permission("dtpunishment.mutepoints.remove")
				.description(Text.of("Add a specified amount of Mutepoints to a player "))
				.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))),
						GenericArguments.onlyOne(GenericArguments.integer(Text.of("amount"))))
				.executor(new CommandMutepointsRemove()).build();

		CommandSpec.Builder builder = CommandSpec.builder().permission("dtpunishment.mutepoints");
		builder.description(Text.of("Show the Mutepoints help menu"));
		builder.child(addMutepoints, "add");
		builder.child(removeMutepoints, "remove");
		builder.child(showMutepoints, "show");

		tryRegisterCommand(builder.build(), mainConfig.aliases.mutepoints);
	}

	private void registerAdminCommands() {

		CommandSpec addWords = CommandSpec.builder().permission("dtpunishment.admin.addwords")
				.description(Text.of("Add a word or words to the list of banned ones"))
				.arguments(GenericArguments.allOf(GenericArguments.string(Text.of("word"))))
				.executor(new CommandWordAdd()).build();

		CommandSpec removeWords = CommandSpec.builder().permission("dtpunishment.admin.removewords")
				.description(Text.of("Remove a word from the list of banned ones"))
				.arguments(GenericArguments.allOf(GenericArguments.string(Text.of("word"))))
				.executor(new CommandWordRemove()).build();

		CommandSpec listWords = CommandSpec.builder().permission("dtpunishment.admin.listwords")
				.description(Text.of("List all banned words"))
				.executor(new CommandWordList()).build();

		CommandSpec unmute = CommandSpec.builder().permission("dtpunishment.admin.unmute")
				.description(Text.of("Unmute a player immediately (removing all mutepoints)"))
				.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))))
				.executor(new CommandUnmute()).build();

		CommandSpec lastOffence = CommandSpec.builder().permission("dtpunishment.admin.lastoffence")
				.description(Text.of("Shows a player's most recent offence"))
				.arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("player"))))
				.executor(new CommandLastOffence()).build();

		CommandSpec reloadConfig = CommandSpec.builder().permission("dtpunishment.admin.reload")
				.description(Text.of("Reload configuration from disk"))
				.executor(new CommandReloadConfig()).build();

		CommandSpec playerInfo = CommandSpec.builder().permission("dtpunishment.playerinfo")
				.description(Text.of("Show your info "))
				.arguments(GenericArguments.onlyOne(GenericArguments.optionalWeak(GenericArguments.requiringPermission(
						GenericArguments.user(Text.of("player")), "dtpunishment.playerinfo.others"))))
				.executor(new CommandPlayerInfo()).build();

		CommandSpec.Builder builder = CommandSpec.builder().permission("dtpunishment.dtp");
		builder.description(Text.of("Admin commands for DTPunishment"));
		builder.child(addWords, "addwords");
		builder.child(removeWords, "removewords");
		builder.child(listWords, "listwords");
		builder.child(reloadConfig, "reloadconfig");
		builder.child(lastOffence, "lastoffence");
		builder.child(unmute, "unmute");
		builder.child(playerInfo, "playerinfo, pinfo");

		tryRegisterCommand(builder.build(), mainConfig.aliases.admin);
	}

	private void unregisterCommands() {
		for (CommandMapping mapping : mappedCommands) {
			spongeCommandManager.removeMapping(mapping);
		}

		mappedCommands.clear();
	}
}
