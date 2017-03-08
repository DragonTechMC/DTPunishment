package me.morpheus.dtpunishment.commands.banpoints;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.utils.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

public class CommandBanpointsShow implements CommandExecutor {

    private DTPunishment main;

    public CommandBanpointsShow(DTPunishment main){
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

        src.sendMessage(Text.of(user.get().getName() + " has " + main.getDatastore().getBanpoints(uuid) + " banpoints"));
        main.getDatastore().finish();

        return CommandResult.success();


    }
}
