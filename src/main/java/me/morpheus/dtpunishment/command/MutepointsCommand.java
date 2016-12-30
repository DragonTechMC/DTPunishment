package me.morpheus.dtpunishment.command;

import me.morpheus.dtpunishment.ConfigUtil;
import me.morpheus.dtpunishment.DBUtils;
import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.PunishmentManager;
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

public class MutepointsCommand implements CommandExecutor {
    private DTPunishment main;

    public MutepointsCommand(DTPunishment main){
        this.main = main;
    }


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(!args.hasAny("player")){
            src.sendMessage(Text.of("Shows the Mute points help menu, I think"));

        }else{

            String name;
            Optional<Player> player = Sponge.getServer().getPlayer(args.getOne("player").get().toString());

            if (player.isPresent()) {
                name = player.get().getName();
            }else{
                Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
                if(userStorage.get().get(args.getOne("player").get().toString()).isPresent()) {
                    name = args.getOne("player").get().toString();
                }else{
                    src.sendMessage(Text.of(args.getOne("player").get().toString() + " never joined your server "));
                    return CommandResult.success();
                }
            }

            Path playerData = Paths.get(main.getConfigPath() + "/data/" + name + ".conf");

            if(args.getOne("player").isPresent() && args.<Integer>getOne("amount").isPresent()){

                PunishmentManager punishment = new PunishmentManager(main);
                int added = args.<Integer>getOne("amount").get();

                if(ConfigUtil.DB_ENABLED) {
                    DBUtils.addMutepoints(player.get().getName(), added);
                    punishment.checkPenalty(player.get().getName(), "mutepoints", DBUtils.getMutepoints(player.get().getName()));
                }else {

                    ConfigurationLoader<CommentedConfigurationNode> loader =
                            HoconConfigurationLoader.builder().setPath(playerData).build();
                    ConfigurationNode rootNode;
                    try {
                        rootNode = loader.load();
                        int actual = rootNode.getNode("points", "mutepoints").getInt();
                        rootNode.getNode("points", "mutepoints").setValue(added + actual);
                        loader.save(rootNode);
                        punishment.checkPenalty(args.getOne("player").get().toString(), "mutepoints",
                                rootNode.getNode("points", "mutepoints").getInt());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }else if(args.getOne("player").isPresent()){
                if(ConfigUtil.DB_ENABLED) {
                    src.sendMessage(Text.of(name + " has " + DBUtils.getMutepoints(name) + " mutepoints"));
                }else {

                    ConfigurationLoader<CommentedConfigurationNode> loader =
                            HoconConfigurationLoader.builder().setPath(playerData).build();
                    ConfigurationNode rootNode;
                    try {
                        rootNode = loader.load();
                        int amount = rootNode.getNode("points", "mutepoints").getInt();
                        src.sendMessage(Text.of(name + " has " + amount + " mutepoints"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }





        }



        return CommandResult.success();
    }
}
