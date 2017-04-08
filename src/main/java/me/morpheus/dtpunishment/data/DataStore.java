package me.morpheus.dtpunishment.data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface DataStore {

    public void init();

    public int getBanpoints(UUID player);

    public LocalDate getBanpointsUpdatedAt(UUID player);

    public int getMutepoints(UUID player);

    public LocalDate getMutepointsUpdatedAt(UUID player);

    public boolean isMuted(UUID player);

    public Instant getExpiration(UUID player);

    public void addBanpoints(UUID player, int amount);

    public void removeBanpoints(UUID player, int amount);

    public void addMutepoints(UUID player, int amount);

    public void removeMutepoints(UUID player, int amount);

    public void mute(UUID player, Instant expiration);

    public void unmute(UUID player);

    public void createUser(UUID player);

    public boolean userExists(UUID player);

    public void finish();
}
