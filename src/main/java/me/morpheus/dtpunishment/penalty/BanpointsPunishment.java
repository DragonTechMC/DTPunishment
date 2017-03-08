package me.morpheus.dtpunishment.penalty;

import me.morpheus.dtpunishment.DTPunishment;
import me.morpheus.dtpunishment.utils.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class BanpointsPunishment {

    private DTPunishment main;

    public BanpointsPunishment(DTPunishment main) {
        this.main = main;
    }


    public void check(UUID uuid, int amount) {

        if (amount < 10) return;

        int rounded = amount/10 * 10;

        String period = main.getConfig().punishment.banpoints.get(rounded + " banpoints");

        int days = Integer.parseInt(period.substring(0, period.length() - 1));

        BanService service = Sponge.getServiceManager().provide(BanService.class).get();

        Instant expiration = Instant.now().plus(Duration.ofDays(days));

        Ban ban = Ban.builder().type(BanTypes.PROFILE).profile(Util.getUser(uuid).get().getProfile())
                .expirationDate(expiration)
                .reason(Text.of(TextColors.AQUA, TextStyles.BOLD, "You have been banned for " + days + " days " +
                        "because you reached " + rounded + " points. "))
                .build();
        service.addBan(ban);

        for (Player pl : Sponge.getServer().getOnlinePlayers()) {
            Text message = Text.builder("[DTP] ").color(TextColors.GOLD).append(
                    Text.builder(Util.getUser(uuid).get().getName() + " has been banned for " + days + " days for exceeding "
                            + rounded + " banpoint(s)").color(TextColors.RED).build()).build();
            pl.sendMessage(message);
        }

        main.getDatastore().removeBanpoints(uuid, rounded);

        if(Util.getUser(uuid).get().isOnline()) {
            Util.getUser(uuid).get().getPlayer().get().kick(Text.of(TextColors.AQUA, TextStyles.BOLD,
                    "You have been banned for " + days + " days " + "because you reached " + rounded + " points. "));
        }


    }


}
