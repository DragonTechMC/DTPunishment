package me.morpheus.dtpunishment.commands.banpoints;

import me.morpheus.dtpunishment.data.DataStore;
import me.morpheus.dtpunishment.penalty.BanpointsPunishment;
import me.morpheus.dtpunishment.utils.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import java.util.UUID;

public class CommandBanpointsAdd implements CommandExecutor {

	@Inject
	private DataStore dataStore;
	
	@Inject
	private BanpointsPunishment banPunish;
	
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User user = args.<User>getOne("player").get();
        UUID uuid = user.getUniqueId();
        String name = user.getName();
        int amount = args.<Integer>getOne("amount").get();

        dataStore.addBanpoints(uuid, amount);

        int post = dataStore.getBanpoints(uuid);

        if (user.isOnline()) {
            user.getPlayer().get().sendMessage(Util.getWatermark().append(Text.of(TextColors.RED, amount + " banpoints have been added; you now have " + post)).build());
        }

    	Text adminMessage = Util.getWatermark().append(
				Text.of(TextColors.RED, String.format("%s has added %d banpoint(s) to %s; they now have %d", src.getName(), amount, name, post))).build();

    	if(src instanceof ConsoleSource)
    		src.sendMessage(adminMessage);
    	
        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            if (p.hasPermission("dtpunishment.staff.notify") || p.getPlayer().get() == src) {
                p.sendMessage(adminMessage);
            }
        }

        banPunish.check(uuid, post);

        dataStore.finish();

        return CommandResult.success();
    }
}
