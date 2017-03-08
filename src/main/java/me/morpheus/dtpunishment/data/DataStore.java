package me.morpheus.dtpunishment.data;

import java.time.Instant;
import java.util.UUID;

public abstract class DataStore {

    public abstract void init();

    public abstract int getBanpoints(UUID player);

    public abstract int getMutepoints(UUID player);

    public abstract boolean isMuted(UUID player);

    public abstract Instant getExpiration(UUID player);

    public abstract boolean hasReceivedBonus(UUID player);

    public abstract void giveBonus(UUID player);

    public abstract void addBanpoints(UUID player, int amount);

    public abstract void removeBanpoints(UUID player, int amount);

    public abstract void addMutepoints(UUID player, int amount);

    public abstract void removeMutepoints(UUID player, int amount);

    public abstract void mute(UUID player, Instant expiration);

    public abstract void unmute(UUID player);

    public abstract void createUser(UUID player);

    public abstract boolean userExists(UUID player);

    public void finish() {

    }

}
