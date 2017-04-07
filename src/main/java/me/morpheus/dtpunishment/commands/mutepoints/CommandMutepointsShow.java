package me.morpheus.dtpunishment.commands.mutepoints;

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

public class CommandMutepointsShow implements CommandExecutor {

    private final DTPunishment main;

    public CommandMutepointsShow(DTPunishment main){
        this.main = main;
    }



    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        Optional<User> user = Util.getUser(args.<String>getOne("player").get());
        if (!user.isPresent()) {
            src.sendMessage(Util.getWatermark().append(Text.of(args.<String>getOne("player").get() + " never joined your server ")).build());
            return CommandResult.empty();
        }

        UUID uuid = user.get().getUniqueId();

        src.sendMessage(Util.getWatermark().append(Text.of(user.get().getName() + " has " + main.getDatastore().getMutepoints(uuid) + " mutepoints")).build());
        main.getDatastore().finish();

        return CommandResult.success();

    }
}
