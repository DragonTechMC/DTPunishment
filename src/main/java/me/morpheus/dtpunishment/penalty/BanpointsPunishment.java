package me.morpheus.dtpunishment.penalty;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import me.morpheus.dtpunishment.configuration.MainConfig;
import me.morpheus.dtpunishment.configuration.Punishment;
import me.morpheus.dtpunishment.utils.Util;

@Singleton
public class BanpointsPunishment {

    @Inject
    private MainConfig mainConfig;

    @Inject
    private Logger logger;

    public void check(UUID uuid, int amount) {

        Punishment punishment = mainConfig.punishments.getApplicableBanpointsPunishment(amount);

        if (punishment == null) {
            logger.info(String.format("No punishment exists for %d banpoints", amount));
            return;
        }

        BanService service = Sponge.getServiceManager().provide(BanService.class).get();

        Instant expiration = Instant.now().plus(punishment.length.duration);

        String durationText = Util.durationToString(punishment.length.duration);

        User user = Util.getUser(uuid).get();

        Ban ban = Ban.builder().type(BanTypes.PROFILE).profile(user.getProfile()).expirationDate(expiration)
                .reason(Util.getWatermark()
                        .append(Text.of(TextColors.AQUA, TextStyles.BOLD,
                                String.format("You have been banned for %s because you exceeded %d points",
                                        durationText, punishment.threshold)))
                        .build())
                .build();
        service.addBan(ban);

        for (Player pl : Sponge.getServer().getOnlinePlayers()) {
            Text message = Util.getWatermark()
                    .append(Text.builder(String.format("%s has been banned for %s for exceeding %d banpoint(s)",
                            user.getName(), durationText, punishment.threshold)).color(TextColors.RED).build())
                    .build();
            pl.sendMessage(message);
        }

        if (user.isOnline()) {
            user.getPlayer().get()
                    .kick(Util.getWatermark()
                            .append(Text.of(TextColors.AQUA, TextStyles.BOLD,
                                    String.format("You have been banned for %s because you exceeded %d points",
                                            durationText, punishment.threshold)))
                            .build());
        }

    }

}
