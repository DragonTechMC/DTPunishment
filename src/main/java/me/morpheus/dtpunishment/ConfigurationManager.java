package me.morpheus.dtpunishment;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.service.sql.SqlService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigurationManager {

    private DTPunishment main;


    public ConfigurationManager(DTPunishment main){
        this.main = main;
    }

    private SqlService sql;

    public javax.sql.DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).get();
        }
        return sql.getDataSource(jdbcUrl);
    }


    public void init() throws SQLException {


        Path defaultConfig = main.getDefaultConfig();
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(defaultConfig).build();
        ConfigurationNode rootNode;
        try {
            rootNode = loader.load();
            if(rootNode.getNode("DB", "Enabled").getBoolean()){
                ConfigUtil.DB_ENABLED = true;
                String name = rootNode.getNode("DB", "Name").getString();
                String host = rootNode.getNode("DB", "Host").getString();
                String port = rootNode.getNode("DB", "Port").getString();
                String user = rootNode.getNode("DB", "Username").getString();
                String pass = rootNode.getNode("DB", "Password").getString();

                String jdbcUrl = "jdbc:mysql://"+user+":"+pass+"@"+host+":"+port+"/"+name;
                Connection conn = getDataSource(jdbcUrl).getConnection();
                DBUtils.JDBC = jdbcUrl;

                try {
                    ResultSet res =  conn.getMetaData().getTables(null, null, "dtpunishment", null);
                    if(!res.next()) {
                        main.getLogger().warn("Found empty db.");
                        main.getLogger().info("Generating tables");
                        conn.prepareStatement("CREATE TABLE dtpunishment\n" +
                                "(\n" +
                                "ID int NOT NULL AUTO_INCREMENT,\n" +
                                "PlayerName varchar(255) NOT NULL,\n" +
                                "Banpoints int(255),\n" +
                                "Mutepoints int(255),\n" +
                                "isMuted boolean,\n" +
                                "Until varchar(255),\n" +
                                "PRIMARY KEY (ID)\n" +
                                ")").execute();
                    }
                } finally {
                    conn.close();
                }



            }else{
                ConfigUtil.DB_ENABLED = false;
                Path dataFolder = Paths.get(main.getConfigPath() + "/data");
                if (dataFolder.toFile().exists()) {
                    main.getLogger().info("Data folder found");
                }else{
                    try {
                        Files.createDirectories(dataFolder);
                    } catch (IOException e) {
                        main.getLogger().error("Can't create the data folder");
                        e.printStackTrace();
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
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
