package me.morpheus.dtpunishment.penalty;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.utils.Util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class MutepointsPunishment {

    private final DTPunishment main;

    public MutepointsPunishment(DTPunishment main) {
        this.main = main;
    }

    public void check(UUID uuid, int amount) {

        if (amount < 5) return;

        int rounded = (amount < 10) ? 5 : amount/10 * 10;

        String punishment = main.getConfig().punishment.mutepoints.get(rounded + " mutepoints");

        if (punishment.substring(0, 1).equalsIgnoreCase("+")) {
            int bp = Integer.parseInt(punishment.substring(1, punishment.length() - 2));

            main.getDatastore().addBanpoints(uuid, bp);

            BanpointsPunishment banpunish = new BanpointsPunishment(main);
            banpunish.check(uuid, bp);
        } else {
            int minutes = Integer.parseInt(punishment.substring(0, punishment.length() - 1));
            Instant expiration = Instant.now().plus(Duration.ofMinutes(minutes));

            main.getDatastore().mute(uuid, expiration);
            main.getDatastore().removeMutepoints(uuid, rounded);

            Player p = Sponge.getServer().getPlayer(uuid).get();

            for (Player pl : Sponge.getServer().getOnlinePlayers()) {
                Text message = Util.getWatermark().append(
                        Text.builder(p.getName() + " has been muted for " + minutes + " minutes for exceeding "
                                + rounded + " mutepoint(s)").color(TextColors.RED).build()).build();
                pl.sendMessage(message);
            }

        }

    }


}
