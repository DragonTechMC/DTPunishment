package me.morpheus.dtpunishment;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class PunishmentManager {

    private DTPunishment main;

    public PunishmentManager(DTPunishment main){
        this.main = main;
    }


    public void check(String p, String pointsType, int amount){


        if(pointsType.equalsIgnoreCase("Banpoints")){
            if(amount < 10) return;
            int rounded = amount/10 * 10;
            Path defaultConfig = main.getDefaultConfig();
            ConfigurationLoader<CommentedConfigurationNode> loader =
                    HoconConfigurationLoader.builder().setPath(defaultConfig).build();
            ConfigurationNode rootNode;
            try {
                rootNode = loader.load();
                String period = rootNode.getNode("punishment", rounded + " banpoints").getString();
                int days = Integer.parseInt(period.substring(0, period.length() - 1));

                BanService service = Sponge.getServiceManager().provide(BanService.class).get();

                Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(p);
                Player player;


                if (onlinePlayer.isPresent()) {
                    player = onlinePlayer.get();
                }else {

                    Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);

                    player = userStorage.get().get(p).get().getPlayer().get();
                }

                Instant expiration = Instant.now().plus(Duration.ofDays(rounded));
                Ban ban = Ban.builder().type(BanTypes.PROFILE).profile(player.getProfile())
                        .expirationDate(expiration)
                        .reason(Text.of("You have been banned for " + period +
                                " days because you reached " + rounded + " points. \nYour ban ends on " + expiration)).build();

                service.addBan(ban);


            } catch (IOException e) {
                e.printStackTrace();
            }


        }else if(pointsType.equalsIgnoreCase("Mutepoints")){







        }





    }




}
