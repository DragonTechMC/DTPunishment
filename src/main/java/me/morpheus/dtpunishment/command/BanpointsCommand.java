package me.morpheus.dtpunishment.command;

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

public class BanpointsCommand implements CommandExecutor {

    private DTPunishment main;

    public BanpointsCommand(DTPunishment main){
        this.main = main;
    }


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {


        if(!(args.hasAny("target") || args.hasAny("player"))){
            src.sendMessage(Text.of("Shows the Ban points help menu, I think"));

        }else{

            main.getLogger().info(args.getOne("player").toString());
            String name;
            Optional<Player> player = null;

            if(args.hasAny("target")){
                player = Sponge.getServer().getPlayer(args.getOne("target").get().toString());
            }else if(args.hasAny("player")){
                player = Sponge.getServer().getPlayer(args.getOne("player").get().toString());
            }

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


                ConfigurationLoader<CommentedConfigurationNode> loader =
                        HoconConfigurationLoader.builder().setPath(playerData).build();
                ConfigurationNode rootNode;
                try {
                    rootNode = loader.load();
                    int added = args.<Integer>getOne("amount").get();
                    int actual = rootNode.getNode("points", "banpoints").getInt();
                    main.getLogger().info(added + actual + "");
                    rootNode.getNode("points", "banpoints").setValue(added + actual);
                    loader.save(rootNode);
                } catch(IOException e) {
                    e.printStackTrace();
                }


            }else if(args.getOne("player").isPresent()){
                ConfigurationLoader<CommentedConfigurationNode> loader =
                        HoconConfigurationLoader.builder().setPath(playerData).build();
                ConfigurationNode rootNode;
                try {
                    rootNode = loader.load();
                    int amount = rootNode.getNode("points", "banpoints").getInt();
                    src.sendMessage(Text.of(name + " has " + amount + " banpoints"));
                } catch(IOException e) {
                    e.printStackTrace();
                }




            }





        }





        src.sendMessage(Text.of("Hello World!"));
        return CommandResult.success();
    }
}
