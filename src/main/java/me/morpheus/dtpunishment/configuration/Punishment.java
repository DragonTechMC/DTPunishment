package me.morpheus.dtpunishment.configuration;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Punishment {

    @Setting(comment = "The amount of banpoints/mutepoints the user must receive to trigger this punishment")
    public int threshold;

    @Setting(comment = "The amount of time to punish the user for (example: 1m - one minute, 2h - two hourds, 5d - five days)")
    public PunishmentLength length;

    @Setting(comment = "The number of banpoints to add when the user reaches this punishment level")
    public int banpoints;

    public Punishment() {
        threshold = 0;
        banpoints = 0;
        length = new PunishmentLength();
    }
}
