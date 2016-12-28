package me.morpheus.dtpunishment;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationManager {

    private DTPunishment main;


    public ConfigurationManager(DTPunishment main){
        this.main = main;
    }

    public void init(){

        Path dataFolder = Paths.get(main.getConfigPath() + "/data");
        if(!dataFolder.toFile().exists()){
            try {
                Files.createDirectories(dataFolder);
            } catch (IOException e) {
                main.getLogger().error("Can't create the data folder");
            }
        }else{
            main.getLogger().info("Data folder found");
        }

    }


    public void generateConfig(){
        if(Files.exists(main.getDefaultConfig())){
            main.getLogger().info("Config found");
        }else{
            main.getLogger().warn("Config not found. Generating default config...");
            Asset asset = Sponge.getAssetManager().getAsset(main, "default.conf").get();
            try {
                asset.copyToFile(main.getDefaultConfig());
                main.getLogger().info("Success");
            } catch (IOException e) {
                main.getLogger().error("Error while creating config");
                e.printStackTrace();
            }
        }
    }




}
