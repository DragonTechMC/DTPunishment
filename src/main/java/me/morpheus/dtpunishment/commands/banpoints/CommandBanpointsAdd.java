package me.morpheus.dtpunishment.commands.banpoints;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.penalty.BanpointsPunishment;
import me.morpheus.dtpunishment.utils.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.UUID;

public class CommandBanpointsAdd implements CommandExecutor {

    private DTPunishment main;

    public CommandBanpointsAdd(DTPunishment main){
        this.main = main;
    }


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<User> user = Util.getUser(args.<String>getOne("player").get());
        if (!user.isPresent()) {
            src.sendMessage(Text.of(args.<String>getOne("player").get() + " never joined your server "));
            return CommandResult.empty();
        }

        UUID uuid = user.get().getUniqueId();
        String name = user.get().getName();
        int amount = args.<Integer>getOne("amount").get();


        main.getDatastore().addBanpoints(uuid, amount);

        int post = main.getDatastore().getBanpoints(uuid);

        if (user.get().isOnline()) {
            user.get().getPlayer().get().sendMessage(Text.of(TextColors.RED, amount + " banpoints have been added; you now have " + post));
        }

        src.sendMessage(Text.of(TextColors.RED, "You have added " + amount + " banpoints to " + name + "; they now have " + post));

        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            if (p.hasPermission("dtpunishment.staff.notify")) {
                Text message = Text.builder("[DTP] ").color(TextColors.GOLD).append(
                        Text.builder(src.getName() + " has added " + amount + " banpoint(s) to "
                                + name +  "; they now have " + post).color(TextColors.RED).build()).build();
                p.sendMessage(message);
            }
        }

        BanpointsPunishment banpunish = new BanpointsPunishment(main);

        banpunish.check(uuid, post);

        main.getDatastore().finish();

        return CommandResult.success();


    }
}
