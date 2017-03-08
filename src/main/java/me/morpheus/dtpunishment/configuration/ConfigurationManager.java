package me.morpheus.dtpunishment.configuration;

import me.morpheus.dtpunishment.DTPunishment;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigurationManager {

    private DTPunishment main;


    public ConfigurationManager(DTPunishment main) {
        this.main = main;
    }


    public void init() {
        main.getLogger().info("Initializing datastore...");
        main.getDatastore().init();
    }


    public void generateConfig() {
        if (Files.notExists(main.getDefaultConfig())) {
            main.getLogger().warn("Config not found. Generating default config...");
            Asset asset = Sponge.getAssetManager().getAsset(main, "default.conf").get();
            try {
                asset.copyToFile(main.getDefaultConfig());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Files.notExists(Paths.get(main.getConfigPath() + "\\chat.conf"))) {
            main.getLogger().warn("Chat config not found. Generating chat config...");
            Asset chat = Sponge.getAssetManager().getAsset(main, "chat.conf").get();
            try {
                chat.copyToDirectory(main.getConfigPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




}
