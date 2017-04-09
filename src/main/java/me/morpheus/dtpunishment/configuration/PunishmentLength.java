package me.morpheus.dtpunishment.configuration;

import java.time.Duration;

import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PunishmentLength {

    public Duration duration;

    public PunishmentLength() {
        duration = Duration.ZERO;
    }
}
