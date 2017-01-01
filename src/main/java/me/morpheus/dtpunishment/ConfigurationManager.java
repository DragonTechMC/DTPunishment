package me.morpheus.dtpunishment;

import me.morpheus.dtpunishment.utils.ConfigUtil;
import me.morpheus.dtpunishment.utils.DBUtil;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

class ConfigurationManager {

    private DTPunishment main;


    ConfigurationManager(DTPunishment main) {
        this.main = main;
    }

    private SqlService sql;

    private DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (!Optional.ofNullable(sql).isPresent()) sql = Sponge.getServiceManager().provide(SqlService.class).get();
        return sql.getDataSource(jdbcUrl);
    }


    void init() throws IOException {

        ConfigurationNode rootNode = main.getDefaultConfigLoader().load();
        if (rootNode.getNode("DB", "Enabled").getBoolean()) {
            ConfigUtil.DB_ENABLED = true;
            String name = rootNode.getNode("DB", "Name").getString();
            String host = rootNode.getNode("DB", "Host").getString();
            String port = rootNode.getNode("DB", "Port").getString();
            String user = rootNode.getNode("DB", "Username").getString();
            String pass = rootNode.getNode("DB", "Password").getString();

            String jdbcUrl = "jdbc:mysql://"+user+":"+pass+"@"+host+":"+port+"/"+name;
            try {
                Connection conn = getDataSource(jdbcUrl).getConnection();
                DBUtil.JDBC = jdbcUrl;
                conn.prepareStatement("CREATE TABLE IF NOT EXISTS dtpunishment\n" +
                        "(\n" +
                        "ID int NOT NULL AUTO_INCREMENT,\n" +
                        "PlayerName varchar(32) NOT NULL,\n" +
                        "Banpoints SMALLINT,\n" +
                        "Mutepoints SMALLINT,\n" +
                        "isMuted boolean,\n" +
                        "Until varchar(255),\n" +
                        "PRIMARY KEY (ID)\n" +
                        ")").executeQuery();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            ConfigUtil.DB_ENABLED = false;
            Path dataFolder = Paths.get(main.getConfigPath() + "/data");
            if (Files.exists(dataFolder)) {
                main.getLogger().info("Data folder found");
            } else {
                Files.createDirectories(dataFolder);
            }
        }
    }




    void generateConfig() {
        if (Files.exists(main.getDefaultConfig())) {
            main.getLogger().info("Config found");
        } else {
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
