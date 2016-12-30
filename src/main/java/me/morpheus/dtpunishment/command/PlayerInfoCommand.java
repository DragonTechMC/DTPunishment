package me.morpheus.dtpunishment.command;

import me.morpheus.dtpunishment.ConfigUtil;
import me.morpheus.dtpunishment.DBUtils;
import me.morpheus.dtpunishment.DTPunishment;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class PlayerInfoCommand implements CommandExecutor {

    private DTPunishment main;

    public PlayerInfoCommand(DTPunishment main){
        this.main = main;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!args.hasAny("player")) {

            if(ConfigUtil.DB_ENABLED) {
                src.sendMessage(Text.of("Your info"));
                src.sendMessage(Text.of("Player : " + src.getName()));
                src.sendMessage(Text.of("Mute : " + DBUtils.getMutepoints(src.getName())));
                src.sendMessage(Text.of("Ban : " + DBUtils.getBanpoints(src.getName())));
            }else {


                Path playerData = Paths.get(main.getConfigPath() + "/data/" + src.getName() + ".conf");
                ConfigurationLoader<CommentedConfigurationNode> loader =
                        HoconConfigurationLoader.builder().setPath(playerData).build();
                ConfigurationNode rootNode;

                try {
                    rootNode = loader.load();
                    int mute = rootNode.getNode("points", "mutepoints").getInt();
                    int ban = rootNode.getNode("points", "banpoints").getInt();

                    src.sendMessage(Text.of("Your info"));
                    src.sendMessage(Text.of("Player : " + src.getName()));
                    src.sendMessage(Text.of("Mute : " + mute));
                    src.sendMessage(Text.of("Ban : " + ban));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {

            String name;
            Optional<Player> player = Sponge.getServer().getPlayer(args.getOne("player").get().toString());

            if (player.isPresent()) {
                name = player.get().getName();
            } else {
                Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
                if (userStorage.get().get(args.getOne("player").get().toString()).isPresent()) {
                    name = args.getOne("player").get().toString();
                } else {
                    src.sendMessage(Text.of(args.getOne("player").get().toString() + " never joined your server "));
                    return CommandResult.success();
                }
            }

            Path playerData = Paths.get(main.getConfigPath() + "/data/" + name + ".conf");


            if (args.getOne("player").isPresent()) {

                if(ConfigUtil.DB_ENABLED) {
                    src.sendMessage(Text.of("Player : " + args.getOne("player").get().toString()));
                    src.sendMessage(Text.of("Mute : " + DBUtils.getMutepoints(name)));
                    src.sendMessage(Text.of("Ban : " + DBUtils.getBanpoints(name)));
                }else {
                    ConfigurationLoader<CommentedConfigurationNode> loader =
                            HoconConfigurationLoader.builder().setPath(playerData).build();
                    ConfigurationNode rootNode;

                    try {
                        rootNode = loader.load();
                        int mute = rootNode.getNode("points", "mutepoints").getInt();
                        int ban = rootNode.getNode("points", "banpoints").getInt();

                        src.sendMessage(Text.of("Player : " + args.getOne("player").get().toString()));
                        src.sendMessage(Text.of("Mute : " + mute));
                        src.sendMessage(Text.of("Ban : " + ban));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

        }

        return CommandResult.success();
    }



}
