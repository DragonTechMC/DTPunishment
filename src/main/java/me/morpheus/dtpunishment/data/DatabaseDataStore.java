package me.morpheus.dtpunishment.data;

import me.morpheus.dtpunishment.DTPunishment;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class DatabaseDataStore extends DataStore {

    private DTPunishment main;

    public DatabaseDataStore(DTPunishment main){
        this.main = main;
    }

    private SqlService sql;

    private DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (!Optional.ofNullable(sql).isPresent()) sql = Sponge.getServiceManager().provide(SqlService.class).get();
        return sql.getDataSource(jdbcUrl);
    }

    private String getJdbcUrl() {
        String name = main.getConfig().database.name;
        String host = main.getConfig().database.host;
        int port = main.getConfig().database.port;
        String user = main.getConfig().database.username;
        String pass = main.getConfig().database.password;

        return "jdbc:mysql://"+user+":"+pass+"@"+host+":"+port+"/"+name;
    }

    private Connection conn;

    @Override
    public void init() {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            conn.prepareStatement("CREATE TABLE IF NOT EXISTS dtpunishment\n" +
                    "(\n" +
                    "ID int NOT NULL AUTO_INCREMENT,\n" +
                    "UUID varchar(64) NOT NULL,\n" +
                    "Banpoints SMALLINT,\n" +
                    "Mutepoints SMALLINT,\n" +
                    "bonus_received boolean,\n" +
                    "isMuted boolean,\n" +
                    "Until varchar(32),\n" +
                    "PRIMARY KEY (ID)\n" +
                    ")").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getBanpoints(UUID player) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            ResultSet set = conn.prepareStatement("SELECT Banpoints FROM dtpunishment WHERE UUID=\"" + player + "\";").executeQuery();
            set.next();
            return set.getInt("Banpoints");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getMutepoints(UUID player) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            ResultSet set = conn.prepareStatement("SELECT Mutepoints FROM dtpunishment WHERE UUID=\""+player+"\";").executeQuery();
            set.next();
            return set.getInt("Mutepoints");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean isMuted(UUID player) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            ResultSet set = conn.prepareStatement("SELECT isMuted FROM dtpunishment WHERE UUID=\""+player+"\";").executeQuery();
            set.next();
            return set.getBoolean("isMuted");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Instant getExpiration(UUID player) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            ResultSet set = conn.prepareStatement("SELECT Until FROM dtpunishment WHERE UUID=\""+player+"\";").executeQuery();
            set.next();
            return Instant.parse(set.getString("Until"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean hasReceivedBonus(UUID player) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            ResultSet set = conn.prepareStatement("SELECT bonus_received FROM dtpunishment WHERE UUID=\""+player+"\";").executeQuery();
            set.next();
            return set.getBoolean("bonus_received");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void giveBonus(UUID player) {

    }

    @Override
    public void addBanpoints(UUID player, int amount) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET Banpoints = Banpoints + "+amount+" WHERE UUID=\""+player+"\";").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void removeBanpoints(UUID player, int amount) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET Banpoints = Banpoints - "+amount+" WHERE UUID=\""+player+"\";").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addMutepoints(UUID player, int amount) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET Mutepoints = Mutepoints + "+amount+" WHERE UUID=\""+player+"\";").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMutepoints(UUID player, int amount) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET Mutepoints = Mutepoints - "+amount+" WHERE UUID=\""+player+"\";").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mute(UUID player, Instant expiration) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET isMuted = 1, Until = "+expiration+" WHERE UUID=\""+player+"\";").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void unmute(UUID player) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            conn.prepareStatement("UPDATE dtpunishment SET isMuted = 0, Until = null WHERE UUID=\""+player+"\";").executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createUser(UUID player) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            conn.prepareStatement("INSERT INTO dtpunishment (UUID,Banpoints,Mutepoints,isMuted)\n" +
                    "VALUES ('"+player+"',0,0,false)").executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean userExists(UUID player) {
        try {
            conn = getDataSource(getJdbcUrl()).getConnection();
            ResultSet set = conn.prepareStatement("SELECT UUID FROM dtpunishment WHERE UUID=\""+player+"\";").executeQuery();
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void finish() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
