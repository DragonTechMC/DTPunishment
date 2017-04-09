package me.morpheus.dtpunishment.configuration;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Punishment {

    @Setting
    public int threshold;

    @Setting
    public PunishmentLength length;

    @Setting
    public int banpoints;

    public Punishment() {
        threshold = 0;
        banpoints = 0;
        length = new PunishmentLength();
    }
}
