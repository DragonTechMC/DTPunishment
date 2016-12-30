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
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class PunishmentManager {

    private DTPunishment main;

    public PunishmentManager(DTPunishment main){
        this.main = main;
    }


    public void checkPenalty(String p, String pointsType, int amount){

        if((pointsType.equalsIgnoreCase("Banpoints") && amount < 10)
            || (pointsType.equalsIgnoreCase("mutepoints") && amount < 5)) {
            return;
        }

        Optional<Player> onlinePlayer = Sponge.getServer().getPlayer(p);
        Player player;

        if (onlinePlayer.isPresent()) {
            player = onlinePlayer.get();
        }else {
            Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
            player = userStorage.get().get(p).get().getPlayer().get();
        }

        Path defaultConfig = main.getDefaultConfig();
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(defaultConfig).build();
        ConfigurationNode rootNode;

        if(pointsType.equalsIgnoreCase("Banpoints")){
            int rounded = amount/10 * 10;

            try {
                rootNode = loader.load();
                String period = rootNode.getNode("punishment", "banpoints", rounded + " banpoints").getString();
                int days = Integer.parseInt(period.substring(0, period.length() - 1));

                BanService service = Sponge.getServiceManager().provide(BanService.class).get();

                Instant expiration = Instant.now().plus(Duration.ofDays(days));

                Ban ban = Ban.builder().type(BanTypes.PROFILE).profile(player.getProfile())
                        .expirationDate(expiration)
                        .reason(Text.of(TextColors.AQUA, TextStyles.BOLD, "You have been banned for " + days + " days " +
                                "because you reached " + rounded + " points. "))
                        .build();


                service.addBan(ban);

                if(ConfigUtil.DB_ENABLED) {
                    DBUtils.addBanpoints(p, -rounded);
                }else {
                    Path playerData = Paths.get(main.getConfigPath() + "/data/" + p + ".conf");

                    ConfigurationLoader<CommentedConfigurationNode> playerLoader =
                            HoconConfigurationLoader.builder().setPath(playerData).build();
                    ConfigurationNode playerNode = playerLoader.load();
                    int actual = playerNode.getNode("points", "banpoints").getInt();

                    playerNode.getNode("points", "banpoints").setValue(actual - rounded);
                    playerLoader.save(playerNode);

                }

                player.kick(Text.of(TextColors.AQUA, TextStyles.BOLD, "You have been banned for " + days + " days " +
                        "because you reached " + rounded + " points. "));

            } catch (IOException e) {
                e.printStackTrace();
            }


        }else if(pointsType.equalsIgnoreCase("Mutepoints")){

            Path playerData = Paths.get(main.getConfigPath() + "/data/" + p + ".conf");

            ConfigurationLoader<CommentedConfigurationNode> playerLoader =
                    HoconConfigurationLoader.builder().setPath(playerData).build();
            ConfigurationNode playerNode;


            if(4 < amount && amount < 10){



                Instant expiration = null;

                try {
                    rootNode = loader.load();
                    String period = rootNode.getNode("punishment", "mutepoints", "5 mutepoints").getString();
                    int minutes = Integer.parseInt(period.substring(0, period.length() - 1));
                    expiration = Instant.now().plus(Duration.ofMinutes(minutes));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(ConfigUtil.DB_ENABLED) {
                    DBUtils.addMutepoints(p, -5);
                    DBUtils.mute(p, String.valueOf(expiration));
                }else {
                    try {
                        playerNode = playerLoader.load();
                        playerNode.getNode("mute", "isMuted").setValue(true);
                        playerNode.getNode("mute", "until").setValue(String.valueOf(expiration));
                        int actual = playerNode.getNode("points", "mutepoints").getInt();

                        playerNode.getNode("points", "mutepoints").setValue(actual - 5);

                        playerLoader.save(playerNode);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else if(amount > 9){
                int rounded = amount/10 * 10;


                try {
                    rootNode = loader.load();
                    String period = rootNode.getNode("punishment", "mutepoints", rounded + " mutepoints").getString();

                    if(period.substring(0, 1).equalsIgnoreCase("+")){

                        int bp = Integer.parseInt(period.substring(1, period.length() - 2));

                        if(ConfigUtil.DB_ENABLED) {
                            DBUtils.addBanpoints(p, bp);
                            checkPenalty(p, "banpoints", DBUtils.getBanpoints(p));
                        }else {
                            playerNode = playerLoader.load();
                            int actual = playerNode.getNode("points", "banpoints").getInt();
                            playerNode.getNode("points", "banpoints").setValue(actual + bp);
                            playerLoader.save(playerNode);
                            checkPenalty(p, "banpoints", playerNode.getNode("points", "banpoints").getInt());

                        }
                    }else{

                        int minutes = Integer.parseInt(period.substring(0, period.length() - 1));
                        Instant expiration = Instant.now().plus(Duration.ofMinutes(minutes));

                        if(ConfigUtil.DB_ENABLED) {
                            DBUtils.mute(p, String.valueOf(expiration));
                            DBUtils.addMutepoints(p, -rounded);

                        }else {
                            playerNode = playerLoader.load();
                            playerNode.getNode("mute", "isMuted").setValue(true);
                            playerNode.getNode("mute", "until").setValue(String.valueOf(expiration));
                            int actual = playerNode.getNode("points", "mutepoints").getInt();

                            playerNode.getNode("points", "mutepoints").setValue(actual - rounded);

                            playerLoader.save(playerNode);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }








        }





    }




}
