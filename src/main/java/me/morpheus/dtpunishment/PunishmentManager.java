package me.morpheus.dtpunishment;

import me.morpheus.dtpunishment.utils.ConfigUtil;
import me.morpheus.dtpunishment.utils.DBUtil;
import me.morpheus.dtpunishment.utils.Util;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class PunishmentManager {

    private DTPunishment main;

    public PunishmentManager(DTPunishment main) {
        this.main = main;
    }


    public void checkPenalty(String p, String pointsType, int amount) {

        if ((pointsType.equalsIgnoreCase("Banpoints") && amount < 10)
            || (pointsType.equalsIgnoreCase("Mutepoints") && amount < 5)) {
            return;
        }

        ConfigurationNode rootNode = null;
        try {
             rootNode = main.getDefaultConfigLoader().load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (pointsType.equalsIgnoreCase("Banpoints")) {
            int rounded = amount/10 * 10;
            String period = rootNode.getNode("punishment", "banpoints", rounded + " banpoints").getString();
            int days = Integer.parseInt(period.substring(0, period.length() - 1));

            BanService service = Sponge.getServiceManager().provide(BanService.class).get();
            Instant expiration = Instant.now().plus(Duration.ofDays(days));
            Ban ban = Ban.builder().type(BanTypes.PROFILE).profile(Util.getUser(p).get().getProfile())
                    .expirationDate(expiration)
                    .reason(Text.of(TextColors.AQUA, TextStyles.BOLD, "You have been banned for " + days + " days " +
                            "because you reached " + rounded + " points. "))
                    .build();
            service.addBan(ban);

            if (ConfigUtil.DB_ENABLED) {
                DBUtil.addBanpoints(p, -rounded);
            } else {
                ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), p);
                int actual = playerNode.getNode("points", "banpoints").getInt();
                playerNode.getNode("points", "banpoints").setValue(actual - rounded);
                ConfigUtil.save(main.getConfigPath(), p, playerNode);
            }

            if(Util.getUser(p).get().isOnline()) {
                Util.getUser(p).get().getPlayer().get().kick(Text.of(TextColors.AQUA, TextStyles.BOLD,
                        "You have been banned for " + days + " days " + "because you reached " + rounded + " points. "));
            }

        } else if (pointsType.equalsIgnoreCase("Mutepoints")) {

            if (4 < amount && amount < 10) {
                String period = rootNode.getNode("punishment", "mutepoints", "5 mutepoints").getString();
                int minutes = Integer.parseInt(period.substring(0, period.length() - 1));
                Instant expiration = Instant.now().plus(Duration.ofMinutes(minutes));
                if (ConfigUtil.DB_ENABLED) {
                    DBUtil.addMutepoints(p, -5);
                    DBUtil.mute(p, String.valueOf(expiration));
                } else {
                    ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), p);
                    playerNode.getNode("mute", "isMuted").setValue(true);
                    playerNode.getNode("mute", "until").setValue(String.valueOf(expiration));
                    int actual = playerNode.getNode("points", "mutepoints").getInt();
                    playerNode.getNode("points", "mutepoints").setValue(actual - 5);
                    ConfigUtil.save(main.getConfigPath(), p, playerNode);
                }
            } else if (amount > 9) {
                int rounded = amount/10 * 10;
                String period = rootNode.getNode("punishment", "mutepoints", rounded + " mutepoints").getString();
                if (period.substring(0, 1).equalsIgnoreCase("+")) {
                    int bp = Integer.parseInt(period.substring(1, period.length() - 2));
                    if (ConfigUtil.DB_ENABLED) {
                        DBUtil.addBanpoints(p, bp);
                        checkPenalty(p, "banpoints", DBUtil.getBanpoints(p));
                    } else {
                        ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), p);
                        int actual = playerNode.getNode("points", "banpoints").getInt();
                        playerNode.getNode("points", "banpoints").setValue(actual + bp);
                        ConfigUtil.save(main.getConfigPath(), p, playerNode);
                        checkPenalty(p, "banpoints", playerNode.getNode("points", "banpoints").getInt());
                    }

                } else {
                    int minutes = Integer.parseInt(period.substring(0, period.length() - 1));
                    Instant expiration = Instant.now().plus(Duration.ofMinutes(minutes));

                    if (ConfigUtil.DB_ENABLED) {
                        DBUtil.mute(p, String.valueOf(expiration));
                        DBUtil.addMutepoints(p, -rounded);
                    } else {
                        ConfigurationNode playerNode = ConfigUtil.getPlayerNode(main.getConfigPath(), p);
                        playerNode.getNode("mute", "isMuted").setValue(true);
                        playerNode.getNode("mute", "until").setValue(String.valueOf(expiration));
                        int actual = playerNode.getNode("points", "mutepoints").getInt();
                        playerNode.getNode("points", "mutepoints").setValue(actual - rounded);
                        ConfigUtil.save(main.getConfigPath(), p, playerNode);
                    }
                }
            }
        }
    }




}
