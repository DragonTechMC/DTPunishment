package me.morpheus.dtpunishment.commands.banpoints;

import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.utils.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import com.google.inject.Inject;

import java.util.UUID;

public class CommandBanpointsShow implements CommandExecutor {

	@Inject
	private DataStore dataStore;

	
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("player").get();
        
        UUID uuid = user.getUniqueId();

        src.sendMessage(Util.getWatermark().append(Text.of(user.getName() + " has " + dataStore.getBanpoints(uuid) + " banpoints")).build());
        dataStore.finish();

        return CommandResult.success();


    }
}
