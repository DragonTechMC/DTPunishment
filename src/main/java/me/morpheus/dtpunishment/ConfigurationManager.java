package me.morpheus.dtpunishment;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

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
        if(!Files.exists(main.getDefaultConfig())){

            Path defaultConfig = main.getDefaultConfig();
            try {
                Files.createFile(defaultConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }


            ConfigurationLoader<CommentedConfigurationNode> loader =
                    HoconConfigurationLoader.builder().setPath(defaultConfig).build();
            ConfigurationNode rootNode;
            try {
                rootNode = loader.load();




                for(int i = 1; i < 8; i++){
                    int amount = i*10;
                    rootNode.getNode("punishment", amount + " banpoints").setValue(i + "d");
                    main.getLogger().info("number " + i);

                }

                rootNode.getNode("punishment", "80 banpoints").setValue("14d");
                rootNode.getNode("punishment", "90 banpoints").setValue("28d");
                rootNode.getNode("punishment", "100 banpoints").setValue("168d");
                rootNode.getNode("punishment", "banpoints", "test").setValue(false);


                loader.save(rootNode);
            } catch(IOException e) {
                e.printStackTrace();
            }



        }


    }




}
