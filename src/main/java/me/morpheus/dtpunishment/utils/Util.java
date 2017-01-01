package me.morpheus.dtpunishment.utils;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Optional;

public class Util {

    public static Optional<User> getUser(String name) {
        Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        if (userStorage.get().get(name).isPresent()) {
            return userStorage.get().get(name);
        } else {
            return Optional.empty();
        }
    }


}
