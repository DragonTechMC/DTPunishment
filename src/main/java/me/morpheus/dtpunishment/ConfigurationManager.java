package me.morpheus.dtpunishment;

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






}
